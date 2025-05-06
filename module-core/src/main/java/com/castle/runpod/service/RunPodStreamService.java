package com.castle.runpod.service;

import com.castle.code.error.MemberErrorCode;
import com.castle.domain.llm.chat.repository.ChatLogRepository;
import com.castle.domain.llm.chat.repository.DialogueRepository;
import com.castle.domain.member.repository.MemberRepository;
import com.castle.exception.MemberException;
import com.castle.runpod.grpc.RunPodGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RunPodStreamService {

    private final RunPodGrpcClient runPodGrpcClient;
    private final MemberRepository memberRepository;
    private final ChatLogRepository chatLogRepository;
    private final DialogueRepository dialogueRepository;

    /**
     * 프론트엔드에 SSE를 통해 스트리밍 전송한다.
     */
    public SseEmitter streamToClient(String prompt, Long memberId, Long dialogueId) {
        if (!Objects.equals(dialogueRepository.findMemberIdByDialogueId(dialogueId), memberId)) {
            throw new MemberException(MemberErrorCode.FORBIDDEN_DIALOGUE_ACCESS);
        }

        SseEmitter emitter = new SseEmitter(0L);

        // 별도 스레드에서 RunPod 호출 및 스트리밍 실행
        new Thread(() -> {
            try {
                String grpcTarget = runPodGrpcClient.runAndGetGrpcTarget(prompt);
                if (grpcTarget == null) {
                    emitter.completeWithError(new IllegalStateException("gRPC target not found"));
                    return;
                }
                runPodGrpcClient.streamFromGrpc(
                        grpcTarget,
                        prompt,
                        token -> {
                            try {
                                emitter.send(SseEmitter.event().name("message").data(token));
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().name("end").data("[생성 완료]"));
                            } catch (Exception ex) {
                                // 완료 전송 실패 시 ignore
                            }
                            emitter.complete();
                        },
                        emitter::completeWithError
                );
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }).start();

        return emitter;
    }
}
