package com.example.influencer.dto;

public class BusinessProfileImageResponse {

	private boolean success;
	private String message;
	private String email;
	private String fileName;

	public BusinessProfileImageResponse() {
	}

	public BusinessProfileImageResponse(boolean success, String message, String email, String fileName) {
		this.success = success;
		this.message = message;
		this.email = email;
		this.fileName = fileName;
	}

	public static BusinessProfileImageResponse stored(String email, String fileName) {
		return new BusinessProfileImageResponse(true, "Business profile image uploaded successfully", email, fileName);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
