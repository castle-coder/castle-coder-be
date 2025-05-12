package com.castle.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChatImage 엔티티 클래스입니다.
 * 이 클래스는 사용자의 채팅에 올라온 이미지을 저장하는 데 사용됩니다.
 */
@Entity
@Table(name = "chat_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_log_id")
    private ChatLog chatLog;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
