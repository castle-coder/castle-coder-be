package com.castle.domain.chat.repository;

import com.castle.domain.chat.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChatLogRepository는 ChatLog 엔티티에 대한 CRUD 작업을 수행하는 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 메서드를 제공합니다.
 */
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
}
