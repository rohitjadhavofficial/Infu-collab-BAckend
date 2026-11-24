package com.example.influencer.dto;

public class UnreadCountResponse {

	private String email;
	private long unreadCount;

	public UnreadCountResponse() {
	}

	public UnreadCountResponse(String email, long unreadCount) {
		this.email = email;
		this.unreadCount = unreadCount;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(long unreadCount) {
		this.unreadCount = unreadCount;
	}
}
