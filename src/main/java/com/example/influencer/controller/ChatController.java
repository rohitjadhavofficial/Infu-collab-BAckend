package com.example.influencer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.influencer.dto.ChatApiResponse;
import com.example.influencer.dto.ChatMessageResponse;
import com.example.influencer.dto.ConversationResponse;
import com.example.influencer.dto.MarkReadRequest;
import com.example.influencer.dto.SendMessageRequest;
import com.example.influencer.dto.UnreadCountResponse;
import com.example.influencer.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatApiResponse<ChatMessageResponse>> sendMessage(
			@Valid @RequestBody SendMessageRequest request) {
		ChatMessageResponse data = chatService.sendMessage(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ChatApiResponse.success("Message sent successfully", data));
	}

	@GetMapping("/messages")
	public ResponseEntity<List<ChatMessageResponse>> getMessages(
			@RequestParam("sourceEmail") String sourceEmail,
			@RequestParam("targetEmail") String targetEmail) {
		return ResponseEntity.ok(chatService.getMessages(sourceEmail, targetEmail));
	}

	@GetMapping("/conversations")
	public ResponseEntity<List<ConversationResponse>> getConversations(@RequestParam("email") String email) {
		return ResponseEntity.ok(chatService.getConversations(email));
	}

	@GetMapping("/unread-count")
	public ResponseEntity<UnreadCountResponse> getUnreadCount(@RequestParam("email") String email) {
		return ResponseEntity.ok(chatService.getUnreadCount(email));
	}

	@PutMapping(value = "/mark-read", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ChatApiResponse<Void>> markRead(@Valid @RequestBody MarkReadRequest request) {
		chatService.markRead(request);
		return ResponseEntity.ok(ChatApiResponse.success("Messages marked as read", null));
	}
}
