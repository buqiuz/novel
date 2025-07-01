package io.github.xxyopen.novel.ai.controller.front;

import io.github.xxyopen.novel.ai.dto.req.TtsRequestBody;
import io.github.xxyopen.novel.ai.service.AiService;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;


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
     * 文生图
     */

    @PostMapping("/textToImage")
    public RestResp<String> textToImage(@RequestBody Map<String, String> request) {

        System.out.println("接收到的请求体: " + request);
        String prompt = request.get("text");


        return RestResp.ok(aiService.textToImage(prompt));
    }

    @PostMapping("/pngToJpg")
    public RestResp<String> pngToJpg(@RequestBody Map<String, String> request) {
        System.out.println("接收到的url: " + request);
        String url = request.get("url");
        return RestResp.ok(aiService.pngToJpg(url));

    }

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
     * 使用 Qwen TTS 合成语音 并以流式方式返回 (POST 版本)
     */
    @PostMapping(value = "/tts/qwen/stream-post", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTtsPost(@RequestBody TtsRequestBody requestBody) {
        return aiService.textToSpeechQwenStream(requestBody.getText(), requestBody.getVoiceType());
    }
}
