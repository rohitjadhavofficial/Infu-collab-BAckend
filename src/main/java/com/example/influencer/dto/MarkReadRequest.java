package com.example.influencer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MarkReadRequest {

	@NotNull(message = "conversationId is required")
	private Long conversationId;

	@NotBlank(message = "email is required")
	@Email(message = "email must be a valid email address")
	@Size(max = 255, message = "email must not exceed 255 characters")
	private String email;

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
