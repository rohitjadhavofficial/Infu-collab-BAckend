package com.example.influencer.dto;

import java.time.Instant;

import com.example.influencer.entity.ChatMessage;

public class ChatMessageResponse {

	private Long id;
	private Long conversationId;
	private String sourceEmail;
	private String targetEmail;
	private String message;
	private Instant sentAt;
	private boolean read;

	public ChatMessageResponse() {
	}

	public static ChatMessageResponse from(ChatMessage message) {
		ChatMessageResponse response = new ChatMessageResponse();
		response.setId(message.getId());
		response.setConversationId(message.getConversation().getId());
		response.setSourceEmail(message.getSourceEmail());
		response.setTargetEmail(message.getTargetEmail());
		response.setMessage(message.getMessage());
		response.setSentAt(message.getSentAt());
		response.setRead(message.isRead());
		return response;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public String getSourceEmail() {
		return sourceEmail;
	}

	public void setSourceEmail(String sourceEmail) {
		this.sourceEmail = sourceEmail;
	}

	public String getTargetEmail() {
		return targetEmail;
	}

	public void setTargetEmail(String targetEmail) {
		this.targetEmail = targetEmail;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(Instant sentAt) {
		this.sentAt = sentAt;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}
}
