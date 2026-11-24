package com.example.influencer.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages", indexes = {
		@Index(name = "idx_chat_messages_conversation_id", columnList = "conversation_id"),
		@Index(name = "idx_chat_messages_target_email", columnList = "target_email"),
		@Index(name = "idx_chat_messages_sent_at", columnList = "sent_at") })
public class ChatMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private ChatConversation conversation;

	@Column(name = "source_email", nullable = false, length = 255)
	private String sourceEmail;

	@Column(name = "target_email", nullable = false, length = 255)
	private String targetEmail;

	@Column(name = "message", nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "sent_at", nullable = false)
	private Instant sentAt;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ChatConversation getConversation() {
		return conversation;
	}

	public void setConversation(ChatConversation conversation) {
		this.conversation = conversation;
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
