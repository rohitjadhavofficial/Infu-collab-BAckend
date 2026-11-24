package com.example.influencer.service;

import com.example.influencer.dto.ChatMessageResponse;

public interface ChatEventPublisher {

	void messageSent(ChatMessageResponse message);
}
