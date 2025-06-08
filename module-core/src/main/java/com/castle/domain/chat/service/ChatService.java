package com.castle.domain.chat.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.domain.chat.dto.request.ChatLogsReadResponse;
import com.castle.domain.chat.dto.request.ChatSessionsReadResponse;
import com.castle.domain.chat.entity.ChatImage;
import com.castle.domain.chat.entity.ChatLog;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatImageRepository;
import com.castle.domain.chat.repository.ChatLogRepository;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.domain.member.entity.Member;
import com.castle.domain.member.repository.MemberRepository;
import com.castle.exception.ChatException;
import com.castle.exception.MemberException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatSessionRepository chatSessionRepository;
    private final MemberRepository memberRepository;
    private final ChatLogRepository chatLogRepository;
    private final ChatImageRepository chatImageRepository;

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

        if (!Objects.equals(chatSession.getMember().getId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }
        List<ChatLog> chatLogs = chatSession.getChatLogs();
        chatLogs.sort(Comparator.comparing(ChatLog::getCreatedAt));

        return chatLogs.stream()
                .map(ChatLogsReadResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long appendChatSession(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER_ID));

        ChatSession chatSession = ChatSession.builder()
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();
        chatSessionRepository.save(chatSession);
        return chatSession.getId();
    }

    @Transactional
    public void updateChatSession(Long memberId, Long chatSessionId, String updateTitle) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        if (!Objects.equals(chatSession.getMember().getId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        chatSession.updateTitle(updateTitle);
        chatSessionRepository.save(chatSession);
    }

    @Transactional
    public void deleteChatSession(Long memberId, Long chatSessionId) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        if (!Objects.equals(chatSession.getMember().getId(), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        chatSessionRepository.delete(chatSession);
    }

    @Transactional
    public void saveChatLog(Long chatSessionId,
                               String prompt,
                               String response,
                               List<String> imageUrls) {
        try {
            ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                    .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

            ChatLog chatLog = ChatLog.builder()
                    .chatSession(chatSession)
                    .member(chatSession.getMember())
                    .prompt(prompt)
                    .response(response)
                    .build();

            chatLogRepository.save(chatLog);

            if (imageUrls != null && !imageUrls.isEmpty()) {
                imageUrls.forEach(url -> chatImageRepository.save(
                        ChatImage.builder()
                                .chatLog(chatLog)
                                .imageUrl(url)
                                .build()
                ));
            }
        } catch (Exception e) {
            log.error("채팅 로그 저장 중 오류 발생", e);
            throw new ChatException(ChatErrorCode.CHAT_LOG_SAVE_FAILED);
        }
    }
}
