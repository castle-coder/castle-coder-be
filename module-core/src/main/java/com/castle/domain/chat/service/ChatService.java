package com.castle.domain.chat.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.domain.chat.dto.request.ChatLogsReadResponse;
import com.castle.domain.chat.dto.request.ChatSessionsReadResponse;
import com.castle.domain.chat.entity.ChatLog;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.exception.ChatException;
import com.castle.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;

    @Transactional(readOnly = true)
    public List<ChatSessionsReadResponse> getChatSessions(Long memberId) {
        List<ChatSession> sessions = chatSessionRepository.findChatSessionsByMemberIdOrderByCreatedAt(memberId);
        return sessions.stream()
                .map(ChatSessionsReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatLogsReadResponse> getChatLogs(Long memberId, Long chatSessionId) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        if (!Objects.equals(chatSession.getMemberId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }
        List<ChatLog> chatLogs = chatSession.getChatLogs();

        return chatLogs.stream()
                .map(ChatLogsReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void appendChatSession(Long memberId) {
        chatSessionRepository.save(ChatSession.builder()
                .memberId(memberId)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void deleteChatSession(Long memberId, Long chatSessionId) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        if (!Objects.equals(chatSession.getMemberId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        chatSessionRepository.delete(chatSession);
    }
}
