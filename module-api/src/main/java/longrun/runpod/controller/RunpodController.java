package longrun.runpod.controller;

import lombok.RequiredArgsConstructor;
import longrun.runpod.service.RunpodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/runpod")
public class RunpodController {

    private final RunpodService runpodService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> submitJobAndStreamOutput(@RequestParam String prompt) {
        return runpodService.streamJob(prompt);
    }

    @GetMapping("/status/{jobId}")
    public Mono<Map> checkJobStatus(@PathVariable String jobId) {
        return runpodService.checkJobStatus(jobId);
    }
}
