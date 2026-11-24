package com.example.influencer.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "chat_conversations", uniqueConstraints = @UniqueConstraint(name = "uk_chat_conversation_pair", columnNames = {
		"user_low", "user_high" }), indexes = @Index(name = "idx_chat_conversations_updated_at", columnList = "updated_at"))
public class ChatConversation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "source_email", nullable = false, length = 255)
	private String sourceEmail;

	@Column(name = "target_email", nullable = false, length = 255)
	private String targetEmail;

	@Column(name = "user_low", nullable = false, length = 255)
	private String userLow;

	@Column(name = "user_high", nullable = false, length = 255)
	private String userHigh;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getUserLow() {
		return userLow;
	}

	public void setUserLow(String userLow) {
		this.userLow = userLow;
	}

	public String getUserHigh() {
		return userHigh;
	}

	public void setUserHigh(String userHigh) {
		this.userHigh = userHigh;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public boolean involves(String email) {
		return email.equalsIgnoreCase(userLow) || email.equalsIgnoreCase(userHigh);
	}

	public String otherParticipant(String email) {
		if (email.equalsIgnoreCase(userLow)) {
			return userHigh;
		}
		if (email.equalsIgnoreCase(userHigh)) {
			return userLow;
		}
		return null;
	}
}
