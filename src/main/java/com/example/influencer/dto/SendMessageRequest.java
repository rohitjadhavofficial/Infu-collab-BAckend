package com.example.influencer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

	@NotBlank(message = "message is required")
	@Size(max = 4000, message = "message must not exceed 4000 characters")
	private String message;

	@NotBlank(message = "sourceEmail is required")
	@Email(message = "sourceEmail must be a valid email address")
	@Size(max = 255, message = "sourceEmail must not exceed 255 characters")
	private String sourceEmail;

	@NotBlank(message = "targetEmail is required")
	@Email(message = "targetEmail must be a valid email address")
	@Size(max = 255, message = "targetEmail must not exceed 255 characters")
	private String targetEmail;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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
}
