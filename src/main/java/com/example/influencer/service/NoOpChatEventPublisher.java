package com.example.influencer.service;

import org.springframework.stereotype.Component;

import com.example.influencer.dto.ChatMessageResponse;

@Component
public class NoOpChatEventPublisher implements ChatEventPublisher {

	@Override
	public void messageSent(ChatMessageResponse message) {
		// Real-time delivery (WebSocket/STOMP) can be added here later without changing send logic.
	}
}
