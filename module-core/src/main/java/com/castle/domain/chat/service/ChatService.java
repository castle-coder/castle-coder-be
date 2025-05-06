package com.castle.domain.chat.service;

import com.castle.domain.chat.dto.request.ChatSessionsReadResponse;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatLogRepository;
import com.castle.domain.chat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;

    public List<ChatSessionsReadResponse> getChatSessions(Long memberId) {
        List<ChatSession> sessions = chatSessionRepository.findChatSessionsByMemberIdOrderByCreatedAt(memberId);
        return sessions.stream()
                .map(ChatSessionsReadResponse::fromEntity) // 또는 new ChatSessionsReadResponse(session)
                .collect(Collectors.toList());
    }
}
