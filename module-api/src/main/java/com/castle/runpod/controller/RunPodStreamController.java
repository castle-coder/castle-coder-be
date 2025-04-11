package com.castle.runpod.controller;

import com.castle.runpod.service.RunPodStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RunPodStreamController {

    private final RunPodStreamService runPodStreamService;

    /**
     * 프론트엔드에서 prompt 파라미터를 받아 SSE 스트림으로 응답 전송
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String prompt) {
        return runPodStreamService.streamToClient(prompt);
    }
}
