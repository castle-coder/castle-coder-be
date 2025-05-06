package com.castle.domain.llm.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialogue 엔티티 클래스입니다.
 * 이 클래스는 채팅기록들의 집합을 저장하는 데 사용됩니다.
 */
@Entity
@Table(name = "dialogue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dialogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title")
    private String title;

    @Column(name = "createAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dialogue", cascade = CascadeType.ALL)
    private List<ChatLog> logs = new ArrayList<>();
}
