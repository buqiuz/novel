package io.github.xxyopen.novel.ai.service;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

public interface AiService {
    String continueText(String text, Double length);
    String expandText(String text, Double ratio);
    String condenseText(String text, Double ratio);
    String polishText(String text);

    String textToSpeech_qwen_tts(String text, String voiceType);

    //语音合成
    Flux<String> textToSpeech_qwen_tts_Flux(String text, String voiceType);

    String textToSpeech_cosyvoice(String text, String voiceType);

    //图片生成
    String textToImage(String text);
    String pngToJpg(String text);

    SseEmitter textToSpeechQwenStream(String text,String voiceType);
}
