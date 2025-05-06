package com.castle.domain.llm.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChatLog 엔티티 클래스입니다.
 * 이 클래스는 사용자의 채팅 기록을 저장하는 데 사용됩니다.
 */
@Entity
@Table(name = "chat_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
