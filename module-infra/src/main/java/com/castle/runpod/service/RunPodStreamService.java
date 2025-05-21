package com.castle.runpod.service;

import com.castle.code.error.ChatErrorCode;
import com.castle.code.error.MemberErrorCode;
import com.castle.code.error.RunPodErrorCode;
import com.castle.domain.chat.entity.ChatImage;
import com.castle.domain.chat.entity.ChatLog;
import com.castle.domain.chat.entity.ChatSession;
import com.castle.domain.chat.repository.ChatImageRepository;
import com.castle.domain.chat.repository.ChatLogRepository;
import com.castle.domain.chat.repository.ChatSessionRepository;
import com.castle.exception.ChatException;
import com.castle.exception.MemberException;
import com.castle.exception.RunPodException;
import com.castle.runpod.grpc.RunPodGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 비동기 스트리밍을 위해 @Async를 사용하도록 리팩터링된 서비스 클래스입니다.
 * 주의: @EnableAsync를 설정 클래스나 메인 애플리케이션 클래스에 추가해야 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RunPodStreamService {

    private final ChatSessionRepository chatSessionRepository;
    private final RunPodAsyncExecutor runPodAsyncExecutor;

    /**
     * 프론트엔드에 SSE를 통해 스트리밍 전송을 시작합니다.
     */
    public SseEmitter streamToClient(String prompt, Long memberId, Long chatSessionId, List<String> imageUrls) {
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_FOUND_CHAT_SESSION));

        Long sessionMemberId = chatSession.getMember().getId();
        if (!Objects.equals(sessionMemberId, memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_CHAT_SESSION_ACCESS);
        }

        // 타임아웃 예: 5분
        SseEmitter emitter = new SseEmitter(300_000L);
        
        // 현재 SecurityContext를 복사하여 전달
        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContext copiedContext = SecurityContextHolder.createEmptyContext();
        copiedContext.setAuthentication(context.getAuthentication());
        
        Authentication auth = context.getAuthentication();
        log.info("호출 당시 Authentication: {}", auth);

        // @Async로 분리된 메소드 호출
        runPodAsyncExecutor.streamAsync(prompt, memberId, chatSessionId, imageUrls, emitter, copiedContext);
        return emitter;
    }
}