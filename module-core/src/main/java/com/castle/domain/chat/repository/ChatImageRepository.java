package com.castle.domain.chat.repository;

import com.castle.domain.chat.entity.ChatImage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChatImageRepository 인터페이스입니다.
 * 이 인터페이스는 ChatImage 엔티티에 대한 CRUD 작업을 수행하는 데 사용됩니다.
 */
public interface ChatImageRepository extends JpaRepository<ChatImage, Long> {
}
