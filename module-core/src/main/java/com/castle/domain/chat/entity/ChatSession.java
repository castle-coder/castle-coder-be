package com.castle.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ChatSession 엔티티 클래스입니다.
 * 이 클래스는 사용자의 대화 세션을 저장하는 데 사용됩니다.
 */
@Entity
@Table(name = "chat_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title")
    private String title = "New Chat";

    @Column(name = "createAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chat_session", cascade = CascadeType.ALL)
    private List<ChatLog> logs = new ArrayList<>();
}
