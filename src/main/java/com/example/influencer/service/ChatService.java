package com.example.influencer.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.influencer.dto.ChatMessageResponse;
import com.example.influencer.dto.ConversationResponse;
import com.example.influencer.dto.MarkReadRequest;
import com.example.influencer.dto.SendMessageRequest;
import com.example.influencer.dto.UnreadCountResponse;
import com.example.influencer.entity.ChatConversation;
import com.example.influencer.entity.ChatMessage;
import com.example.influencer.exception.ChatException;
import com.example.influencer.repository.BusinessLoginRepository;
import com.example.influencer.repository.ChatConversationRepository;
import com.example.influencer.repository.ChatMessageRepository;
import com.example.influencer.repository.InfluencerLoginRepository;

@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final ChatConversationRepository chatConversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final InfluencerLoginRepository influencerLoginRepository;
	private final BusinessLoginRepository businessLoginRepository;
	private final ChatEventPublisher chatEventPublisher;

	public ChatService(ChatConversationRepository chatConversationRepository,
			ChatMessageRepository chatMessageRepository, InfluencerLoginRepository influencerLoginRepository,
			BusinessLoginRepository businessLoginRepository, ChatEventPublisher chatEventPublisher) {
		this.chatConversationRepository = chatConversationRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.influencerLoginRepository = influencerLoginRepository;
		this.businessLoginRepository = businessLoginRepository;
		this.chatEventPublisher = chatEventPublisher;
	}

	@Transactional
	public ChatMessageResponse sendMessage(SendMessageRequest request) {
		String sourceEmail = requireValidEmail(request.getSourceEmail(), "sourceEmail");
		String targetEmail = requireValidEmail(request.getTargetEmail(), "targetEmail");
		String messageText = requireMessage(request.getMessage());
		if (sourceEmail.equals(targetEmail)) {
			throw new ChatException(HttpStatus.BAD_REQUEST, "Cannot send a message to yourself");
		}
		if (!userExists(sourceEmail)) {
			throw new ChatException(HttpStatus.BAD_REQUEST, "Sender not found");
		}
		if (!userExists(targetEmail)) {
			throw new ChatException(HttpStatus.BAD_REQUEST, "Recipient not found");
		}

		ChatConversation conversation = findOrCreateConversation(sourceEmail, targetEmail);
		Instant sentAt = Instant.now();

		ChatMessage message = new ChatMessage();
		message.setConversation(conversation);
		message.setSourceEmail(sourceEmail);
		message.setTargetEmail(targetEmail);
		message.setMessage(messageText);
		message.setSentAt(sentAt);
		message.setRead(false);
		ChatMessage saved = chatMessageRepository.saveAndFlush(message);

		conversation.setUpdatedAt(sentAt);
		chatConversationRepository.save(conversation);

		ChatMessageResponse response = ChatMessageResponse.from(saved);
		publishAfterCommit(response);
		log.info("Chat message sent from {} to {}", sourceEmail, targetEmail);
		return response;
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> getMessages(String sourceEmail, String targetEmail) {
		String normalizedSource = requireValidEmail(sourceEmail, "sourceEmail");
		String normalizedTarget = requireValidEmail(targetEmail, "targetEmail");
		if (normalizedSource.equals(normalizedTarget)) {
			throw new ChatException(HttpStatus.BAD_REQUEST, "Cannot load a conversation with yourself");
		}

		return findConversation(normalizedSource, normalizedTarget)
				.map(conversation -> chatMessageRepository.findByConversation_IdOrderBySentAtAsc(conversation.getId())
						.stream()
						.map(ChatMessageResponse::from)
						.toList())
				.orElseGet(List::of);
	}

	@Transactional(readOnly = true)
	public List<ConversationResponse> getConversations(String email) {
		String normalizedEmail = requireValidEmail(email, "email");
		List<ChatConversation> conversations = chatConversationRepository
				.findByUserLowOrUserHighOrderByUpdatedAtDesc(normalizedEmail, normalizedEmail);
		if (conversations.isEmpty()) {
			return List.of();
		}

		List<Long> conversationIds = conversations.stream().map(ChatConversation::getId).toList();
		Map<Long, ChatMessage> latestByConversation = latestMessages(conversationIds);
		Map<Long, Long> unreadByConversation = unreadCounts(conversationIds, normalizedEmail);

		List<ConversationResponse> responses = new ArrayList<>(conversations.size());
		for (ChatConversation conversation : conversations) {
			ChatMessage latest = latestByConversation.get(conversation.getId());
			responses.add(new ConversationResponse(
					conversation.getId(),
					conversation.otherParticipant(normalizedEmail),
					latest == null ? null : latest.getMessage(),
					latest == null ? conversation.getUpdatedAt() : latest.getSentAt(),
					unreadByConversation.getOrDefault(conversation.getId(), 0L)));
		}
		return responses;
	}

	@Transactional(readOnly = true)
	public UnreadCountResponse getUnreadCount(String email) {
		String normalizedEmail = requireValidEmail(email, "email");
		long unreadCount = chatMessageRepository.countByTargetEmailIgnoreCaseAndReadFalse(normalizedEmail);
		return new UnreadCountResponse(normalizedEmail, unreadCount);
	}

	@Transactional
	public void markRead(MarkReadRequest request) {
		String email = requireValidEmail(request.getEmail(), "email");
		ChatConversation conversation = chatConversationRepository.findById(request.getConversationId())
				.orElseThrow(() -> new ChatException(HttpStatus.NOT_FOUND, "Conversation not found"));
		if (!conversation.involves(email)) {
			throw new ChatException(HttpStatus.FORBIDDEN, "Not authorized to update this conversation");
		}
		chatMessageRepository.markReceivedMessagesRead(conversation.getId(), email);
	}

	private Map<Long, ChatMessage> latestMessages(List<Long> conversationIds) {
		Map<Long, ChatMessage> latestByConversation = new HashMap<>();
		for (ChatMessage message : chatMessageRepository.findByConversation_IdInOrderBySentAtDesc(conversationIds)) {
			latestByConversation.putIfAbsent(message.getConversation().getId(), message);
		}
		return latestByConversation;
	}

	private Map<Long, Long> unreadCounts(List<Long> conversationIds, String email) {
		Map<Long, Long> unreadByConversation = new HashMap<>();
		for (Object[] row : chatMessageRepository.countUnreadByConversationIds(conversationIds, email)) {
			unreadByConversation.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
		}
		return unreadByConversation;
	}

	private ChatConversation findOrCreateConversation(String sourceEmail, String targetEmail) {
		return findConversation(sourceEmail, targetEmail).orElseGet(() -> createConversation(sourceEmail, targetEmail));
	}

	private java.util.Optional<ChatConversation> findConversation(String firstEmail, String secondEmail) {
		String userLow = minEmail(firstEmail, secondEmail);
		String userHigh = maxEmail(firstEmail, secondEmail);
		return chatConversationRepository.findByUserLowAndUserHigh(userLow, userHigh);
	}

	private ChatConversation createConversation(String sourceEmail, String targetEmail) {
		String userLow = minEmail(sourceEmail, targetEmail);
		String userHigh = maxEmail(sourceEmail, targetEmail);
		Instant now = Instant.now();

		ChatConversation conversation = new ChatConversation();
		conversation.setSourceEmail(sourceEmail);
		conversation.setTargetEmail(targetEmail);
		conversation.setUserLow(userLow);
		conversation.setUserHigh(userHigh);
		conversation.setCreatedAt(now);
		conversation.setUpdatedAt(now);
		try {
			return chatConversationRepository.saveAndFlush(conversation);
		} catch (DataIntegrityViolationException ex) {
			return chatConversationRepository.findByUserLowAndUserHigh(userLow, userHigh)
					.orElseThrow(() -> new ChatException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Unable to create conversation"));
		}
	}

	private boolean userExists(String email) {
		return influencerLoginRepository.existsByEmailIgnoreCase(email)
				|| businessLoginRepository.existsByEmailIgnoreCase(email);
	}

	private String requireMessage(String message) {
		if (message == null || message.isBlank()) {
			throw new ChatException(HttpStatus.BAD_REQUEST, "message is required");
		}
		return message.trim();
	}

	private String requireValidEmail(String email, String fieldName) {
		if (email == null || email.isBlank()) {
			throw new ChatException(HttpStatus.BAD_REQUEST, fieldName + " is required");
		}
		String normalized = email.trim().toLowerCase(Locale.ROOT);
		if (!EMAIL_PATTERN.matcher(normalized).matches()) {
			throw new ChatException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid email address");
		}
		return normalized;
	}

	private String minEmail(String first, String second) {
		return first.compareTo(second) <= 0 ? first : second;
	}

	private String maxEmail(String first, String second) {
		return first.compareTo(second) <= 0 ? second : first;
	}

	private void publishAfterCommit(ChatMessageResponse response) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					chatEventPublisher.messageSent(response);
				}
			});
			return;
		}
		chatEventPublisher.messageSent(response);
	}
}
