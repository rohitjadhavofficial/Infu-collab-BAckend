package com.example.influencer.dto;

public class ApiResponse {

	private String status;
	private String message;

	public ApiResponse() {
	}

	public ApiResponse(String status, String message) {
		this.status = status;
		this.message = message;
	}

	public static ApiResponse ok(String message) {
		return new ApiResponse("OK", message);
	}

	public static ApiResponse error(String message) {
		return new ApiResponse("ERROR", message);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
