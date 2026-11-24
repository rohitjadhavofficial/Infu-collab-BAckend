package com.example.influencer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.influencer.dto.ChatMessageResponse;
import com.example.influencer.dto.ConversationResponse;
import com.example.influencer.dto.MarkReadRequest;
import com.example.influencer.dto.SendMessageRequest;
import com.example.influencer.dto.UnreadCountResponse;
import com.example.influencer.exception.ChatException;
import com.example.influencer.exception.GlobalExceptionHandler;
import com.example.influencer.service.ChatService;

@WebMvcTest({ ChatController.class, GlobalExceptionHandler.class })
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ChatService chatService;

	@Test
	void sendMessageReturnsCreated() throws Exception {
		ChatMessageResponse data = sampleMessage();
		when(chatService.sendMessage(any(SendMessageRequest.class))).thenReturn(data);

		mockMvc.perform(post("/api/chat/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "message": "Hello, I am interested in your advertising services.",
						  "sourceEmail": "business@gmail.com",
						  "targetEmail": "influencer@gmail.com"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Message sent successfully"))
				.andExpect(jsonPath("$.data.id").value(101))
				.andExpect(jsonPath("$.data.conversationId").value(10))
				.andExpect(jsonPath("$.data.sourceEmail").value("business@gmail.com"))
				.andExpect(jsonPath("$.data.targetEmail").value("influencer@gmail.com"))
				.andExpect(jsonPath("$.data.read").value(false));
	}

	@Test
	void sendMessageReturnsBadRequestForEmptyMessage() throws Exception {
		mockMvc.perform(post("/api/chat/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "message": "   ",
						  "sourceEmail": "business@gmail.com",
						  "targetEmail": "influencer@gmail.com"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").isString());
	}

	@Test
	void sendMessageReturnsBadRequestWhenSenderIsMissing() throws Exception {
		when(chatService.sendMessage(any(SendMessageRequest.class)))
				.thenThrow(new ChatException(HttpStatus.BAD_REQUEST, "Sender not found"));

		mockMvc.perform(post("/api/chat/send")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "message": "Hello",
						  "sourceEmail": "missing@gmail.com",
						  "targetEmail": "influencer@gmail.com"
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Sender not found"));
	}

	@Test
	void getMessagesReturnsConversationHistory() throws Exception {
		when(chatService.getMessages("business@gmail.com", "influencer@gmail.com"))
				.thenReturn(List.of(sampleMessage()));

		mockMvc.perform(get("/api/chat/messages")
				.param("sourceEmail", "business@gmail.com")
				.param("targetEmail", "influencer@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(101))
				.andExpect(jsonPath("$[0].message").value("Hello, I am interested in your advertising services."));
	}

	@Test
	void getConversationsReturnsSummaries() throws Exception {
		when(chatService.getConversations("business@gmail.com"))
				.thenReturn(List.of(new ConversationResponse(10L, "influencer@gmail.com", "Hello",
						Instant.parse("2026-10-20T14:30:00Z"), 2L)));

		mockMvc.perform(get("/api/chat/conversations").param("email", "business@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].conversationId").value(10))
				.andExpect(jsonPath("$[0].otherParticipantEmail").value("influencer@gmail.com"))
				.andExpect(jsonPath("$[0].unreadCount").value(2));
	}

	@Test
	void getUnreadCountReturnsTotal() throws Exception {
		when(chatService.getUnreadCount("influencer@gmail.com"))
				.thenReturn(new UnreadCountResponse("influencer@gmail.com", 3L));

		mockMvc.perform(get("/api/chat/unread-count").param("email", "influencer@gmail.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("influencer@gmail.com"))
				.andExpect(jsonPath("$.unreadCount").value(3));
	}

	@Test
	void markReadReturnsSuccess() throws Exception {
		mockMvc.perform(put("/api/chat/mark-read")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "conversationId": 10,
						  "email": "influencer@gmail.com"
						}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Messages marked as read"));
	}

	@Test
	void markReadReturnsForbiddenWhenUnauthorized() throws Exception {
		doThrow(new ChatException(HttpStatus.FORBIDDEN, "Not authorized to update this conversation"))
				.when(chatService).markRead(any(MarkReadRequest.class));

		mockMvc.perform(put("/api/chat/mark-read")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "conversationId": 10,
						  "email": "other@gmail.com"
						}
						"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.success").value(false));
	}

	private ChatMessageResponse sampleMessage() {
		ChatMessageResponse data = new ChatMessageResponse();
		data.setId(101L);
		data.setConversationId(10L);
		data.setSourceEmail("business@gmail.com");
		data.setTargetEmail("influencer@gmail.com");
		data.setMessage("Hello, I am interested in your advertising services.");
		data.setSentAt(Instant.parse("2026-10-20T14:30:00Z"));
		data.setRead(false);
		return data;
	}
}
