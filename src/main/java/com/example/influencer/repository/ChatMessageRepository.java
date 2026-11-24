package com.example.influencer.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.influencer.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findByConversation_IdOrderBySentAtAsc(Long conversationId);

	List<ChatMessage> findByConversation_IdInOrderBySentAtDesc(Collection<Long> conversationIds);

	long countByTargetEmailIgnoreCaseAndReadFalse(String email);

	@Query("""
			select m.conversation.id, count(m)
			from ChatMessage m
			where m.conversation.id in :conversationIds
			  and lower(m.targetEmail) = lower(:email)
			  and m.read = false
			group by m.conversation.id
			""")
	List<Object[]> countUnreadByConversationIds(@Param("conversationIds") Collection<Long> conversationIds,
			@Param("email") String email);

	@Modifying(clearAutomatically = true)
	@Query("""
			update ChatMessage m
			set m.read = true
			where m.conversation.id = :conversationId
			  and lower(m.targetEmail) = lower(:email)
			  and m.read = false
			""")
	int markReceivedMessagesRead(@Param("conversationId") Long conversationId, @Param("email") String email);
}
