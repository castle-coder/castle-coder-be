package com.castle.domain.chat.repository;

import com.castle.domain.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * ChatSessionRepository는 ChatSession 엔티티에 대한 CRUD 작업을 수행하는 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 메서드를 제공합니다.
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Long findMemberIdById(Long chatSessionId);

    /**
     * 주어진 memberId에 해당하는 ChatSession을 생성일자(createdAt) 기준으로 내림차순 정렬하여 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 해당 회원의 ChatSession 목록
     */
    List<ChatSession> findChatSessionsByMemberIdOrderByCreatedAt(Long memberId);
}
