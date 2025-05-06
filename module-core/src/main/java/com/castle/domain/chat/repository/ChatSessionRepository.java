package com.castle.domain.chat.repository;

import com.castle.domain.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChatSessionRepository는 ChatSession 엔티티에 대한 CRUD 작업을 수행하는 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 메서드를 제공합니다.
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * 주어진 chatSessionId에 해당하는 ChatSession의 memberId를 조회합니다.
     *
     * @param chatSessionId 조회할 ChatSession의 ID
     * @return 해당 ChatSession의 memberId
     */
    Long findMemberIdByChatSessionId(Long chatSessionId);
}
