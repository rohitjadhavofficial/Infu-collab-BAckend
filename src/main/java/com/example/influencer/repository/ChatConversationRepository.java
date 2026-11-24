package com.example.influencer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.influencer.entity.ChatConversation;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

	Optional<ChatConversation> findByUserLowAndUserHigh(String userLow, String userHigh);

	List<ChatConversation> findByUserLowOrUserHighOrderByUpdatedAtDesc(String userLow, String userHigh);
}
