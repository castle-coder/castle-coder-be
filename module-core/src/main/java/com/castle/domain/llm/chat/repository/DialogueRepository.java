package com.castle.domain.llm.chat.repository;

import com.castle.domain.llm.chat.entity.Dialogue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DialogueRepository는 Dialogue 엔티티에 대한 CRUD 작업을 수행하는 인터페이스입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 메서드를 제공합니다.
 */
public interface DialogueRepository extends JpaRepository<Dialogue, Long> {

    /**
     * 대화 ID로 회원 ID를 조회하는 메서드입니다.
     *
     * @param dialogueId 대화 ID
     * @return 회원 ID
     */
    Long findMemberIdByDialogueId(Long dialogueId);
}
