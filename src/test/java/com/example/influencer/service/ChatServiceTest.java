package com.example.influencer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.example.influencer.dto.ChatMessageResponse;
import com.example.influencer.dto.ConversationResponse;
import com.example.influencer.dto.MarkReadRequest;
import com.example.influencer.dto.SendMessageRequest;
import com.example.influencer.entity.BusinessLogin;
import com.example.influencer.entity.InfluencerLogin;
import com.example.influencer.exception.ChatException;
import com.example.influencer.repository.BusinessLoginRepository;
import com.example.influencer.repository.ChatConversationRepository;
import com.example.influencer.repository.InfluencerLoginRepository;

@SpringBootTest
@Transactional
class ChatServiceTest {

	@Autowired
	private ChatService chatService;

	@Autowired
	private InfluencerLoginRepository influencerLoginRepository;

	@Autowired
	private BusinessLoginRepository businessLoginRepository;

	@Autowired
	private ChatConversationRepository chatConversationRepository;

	@Test
	void sendMessageCreatesConversationAndPersistsMessage() {
		String suffix = String.valueOf(System.nanoTime());
		String businessEmail = "biz.send." + suffix + "@gmail.com";
		String influencerEmail = "inf.send." + suffix + "@gmail.com";
		saveBusiness(businessEmail);
		saveInfluencer(influencerEmail);

		ChatMessageResponse first = chatService.sendMessage(request(
				"Hi, I am interested in promoting my business through your profile.",
				businessEmail, influencerEmail));

		assertThat(first.getId()).isNotNull();
		assertThat(first.getConversationId()).isNotNull();
		assertThat(first.isRead()).isFalse();
		assertThat(first.getSourceEmail()).isEqualTo(businessEmail);
		assertThat(first.getTargetEmail()).isEqualTo(influencerEmail);

		ChatMessageResponse reply = chatService.sendMessage(request("Thanks, I am available next week.",
				influencerEmail, businessEmail));

		assertThat(reply.getConversationId()).isEqualTo(first.getConversationId());
		assertThat(chatConversationRepository.count()).isGreaterThanOrEqualTo(1);
		assertThat(chatService.getMessages(influencerEmail, businessEmail)).hasSize(2);
		assertThat(chatService.getMessages(businessEmail, influencerEmail).get(0).getMessage())
				.contains("interested in promoting");
	}

	@Test
	void conversationListIncludesLatestMessageAndUnreadCount() {
		String suffix = String.valueOf(System.nanoTime());
		String businessEmail = "biz.list." + suffix + "@gmail.com";
		String influencerEmail = "inf.list." + suffix + "@gmail.com";
		saveBusiness(businessEmail);
		saveInfluencer(influencerEmail);

		chatService.sendMessage(request("First", businessEmail, influencerEmail));
		ChatMessageResponse second = chatService.sendMessage(request("Second", businessEmail, influencerEmail));

		ConversationResponse summary = chatService.getConversations(influencerEmail).get(0);
		assertThat(summary.getConversationId()).isEqualTo(second.getConversationId());
		assertThat(summary.getOtherParticipantEmail()).isEqualTo(businessEmail);
		assertThat(summary.getLatestMessage()).isEqualTo("Second");
		assertThat(summary.getUnreadCount()).isEqualTo(2);

		assertThat(chatService.getUnreadCount(influencerEmail).getUnreadCount()).isEqualTo(2);
		assertThat(chatService.getUnreadCount(businessEmail).getUnreadCount()).isZero();
	}

	@Test
	void markReadOnlyUpdatesMessagesReceivedByUser() {
		String suffix = String.valueOf(System.nanoTime());
		String businessEmail = "biz.read." + suffix + "@gmail.com";
		String influencerEmail = "inf.read." + suffix + "@gmail.com";
		saveBusiness(businessEmail);
		saveInfluencer(influencerEmail);

		ChatMessageResponse inbound = chatService.sendMessage(request("Hello", businessEmail, influencerEmail));
		chatService.sendMessage(request("Reply", influencerEmail, businessEmail));

		MarkReadRequest markReadRequest = new MarkReadRequest();
		markReadRequest.setConversationId(inbound.getConversationId());
		markReadRequest.setEmail(influencerEmail);
		chatService.markRead(markReadRequest);

		assertThat(chatService.getUnreadCount(influencerEmail).getUnreadCount()).isZero();
		assertThat(chatService.getUnreadCount(businessEmail).getUnreadCount()).isEqualTo(1);
		assertThat(chatService.getMessages(businessEmail, influencerEmail).stream()
				.filter(message -> influencerEmail.equals(message.getTargetEmail()))
				.allMatch(ChatMessageResponse::isRead)).isTrue();
		assertThat(chatService.getMessages(businessEmail, influencerEmail).stream()
				.filter(message -> businessEmail.equals(message.getTargetEmail()))
				.noneMatch(ChatMessageResponse::isRead)).isTrue();
	}

	@Test
	void sendMessageRejectsUnknownUsersEmptyTextAndSelfMessaging() {
		String suffix = String.valueOf(System.nanoTime());
		String businessEmail = "biz.invalid." + suffix + "@gmail.com";
		saveBusiness(businessEmail);

		assertThatThrownBy(() -> chatService.sendMessage(request("Hello", businessEmail, "missing@gmail.com")))
				.isInstanceOf(ChatException.class)
				.hasMessage("Recipient not found")
				.extracting(ex -> ((ChatException) ex).getStatus())
				.isEqualTo(HttpStatus.BAD_REQUEST);

		assertThatThrownBy(() -> chatService.sendMessage(request("Hello", "missing@gmail.com", businessEmail)))
				.isInstanceOf(ChatException.class)
				.hasMessage("Sender not found");

		assertThatThrownBy(() -> chatService.sendMessage(request("Hello", businessEmail, businessEmail)))
				.isInstanceOf(ChatException.class)
				.hasMessage("Cannot send a message to yourself");

		assertThatThrownBy(() -> chatService.sendMessage(request("   ", businessEmail, "inf@gmail.com")))
				.isInstanceOf(ChatException.class)
				.hasMessage("message is required");
	}

	@Test
	void markReadRejectsUsersOutsideTheConversation() {
		String suffix = String.valueOf(System.nanoTime());
		String businessEmail = "biz.auth." + suffix + "@gmail.com";
		String influencerEmail = "inf.auth." + suffix + "@gmail.com";
		String outsiderEmail = "out.auth." + suffix + "@gmail.com";
		saveBusiness(businessEmail);
		saveInfluencer(influencerEmail);
		saveInfluencer(outsiderEmail);

		ChatMessageResponse sent = chatService.sendMessage(request("Hello", businessEmail, influencerEmail));
		MarkReadRequest markReadRequest = new MarkReadRequest();
		markReadRequest.setConversationId(sent.getConversationId());
		markReadRequest.setEmail(outsiderEmail);

		assertThatThrownBy(() -> chatService.markRead(markReadRequest))
				.isInstanceOf(ChatException.class)
				.hasMessage("Not authorized to update this conversation")
				.extracting(ex -> ((ChatException) ex).getStatus())
				.isEqualTo(HttpStatus.FORBIDDEN);
	}

	private SendMessageRequest request(String message, String sourceEmail, String targetEmail) {
		SendMessageRequest sendMessageRequest = new SendMessageRequest();
		sendMessageRequest.setMessage(message);
		sendMessageRequest.setSourceEmail(sourceEmail);
		sendMessageRequest.setTargetEmail(targetEmail);
		return sendMessageRequest;
	}

	private void saveBusiness(String email) {
		BusinessLogin business = new BusinessLogin();
		business.setBusinessName("Test Business");
		business.setCategory("Food");
		business.setCity("Solapur");
		business.setEmail(email);
		business.setPhone("9876543210");
		business.setPassword("hashed-password");
		businessLoginRepository.saveAndFlush(business);
	}

	private void saveInfluencer(String email) {
		InfluencerLogin influencer = new InfluencerLogin();
		influencer.setEmail(email);
		influencer.setName("Test Influencer");
		influencerLoginRepository.saveAndFlush(influencer);
	}
}
