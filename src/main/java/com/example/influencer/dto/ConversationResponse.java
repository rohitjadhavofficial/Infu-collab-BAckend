package com.example.influencer.dto;

import java.time.Instant;

public class ConversationResponse {

	private Long conversationId;
	private String otherParticipantEmail;
	private String latestMessage;
	private Instant latestMessageAt;
	private long unreadCount;

	public ConversationResponse() {
	}

	public ConversationResponse(Long conversationId, String otherParticipantEmail, String latestMessage,
			Instant latestMessageAt, long unreadCount) {
		this.conversationId = conversationId;
		this.otherParticipantEmail = otherParticipantEmail;
		this.latestMessage = latestMessage;
		this.latestMessageAt = latestMessageAt;
		this.unreadCount = unreadCount;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public String getOtherParticipantEmail() {
		return otherParticipantEmail;
	}

	public void setOtherParticipantEmail(String otherParticipantEmail) {
		this.otherParticipantEmail = otherParticipantEmail;
	}

	public String getLatestMessage() {
		return latestMessage;
	}

	public void setLatestMessage(String latestMessage) {
		this.latestMessage = latestMessage;
	}

	public Instant getLatestMessageAt() {
		return latestMessageAt;
	}

	public void setLatestMessageAt(Instant latestMessageAt) {
		this.latestMessageAt = latestMessageAt;
	}

	public long getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(long unreadCount) {
		this.unreadCount = unreadCount;
	}
}
