package io.github.xxyopen.novel.ai.controller.front;

import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import io.github.xxyopen.novel.ai.service.AiService;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.reactivex.Flowable;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;


/**
 * <p>
 * 前台门户-Ai相关 API 控制器
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-27 10:24:12
 */
@Tag(name = "FrontAiController", description = "前台门户-AI模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_AI_URL_PREFIX)
@RequiredArgsConstructor
public class FrontAiController {

    private final AiService aiService;

    /**
     * 使用 Qwen TTS 合成语音
     */
    @PostMapping("/tts/qwen")
    public RestResp<String> textToSpeechQwenTts(
            @RequestParam("text") String text,
            @RequestParam("voiceType") String voiceType) {
        return RestResp.ok(aiService.textToSpeech_qwen_tts(text, voiceType));
    }

    /**
     * 使用 CosyVoice 合成语音
     */
    @PostMapping("/tts/cosy")
    public RestResp<String> textToSpeechCosyVoice(
            @RequestParam("text") String text,
            @RequestParam("voiceType") String voiceType) {
        return RestResp.ok(aiService.textToSpeech_cosyvoice(text, voiceType));
    }

//    /**
//     * 使用 Qwen TTS 合成语音 并以流式方式返回
//     */
//    @GetMapping(value = "/tts/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> streamTextToSpeech(@RequestParam String text, @RequestParam String voiceType) {
//        return aiService.textToSpeech_qwen_tts_Flux(text, voiceType);
//    }
    /**
     * 使用 Qwen TTS 合成语音 并以流式方式返回
     */
    @CrossOrigin( origins = "*")
    @GetMapping(value = "/tts/qwen/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTts(
            @RequestParam String text,
            @RequestParam(defaultValue = "CHERRY") String voiceType
    ) {
        return aiService.textToSpeechQwenStream(text, voiceType);
    }

}
