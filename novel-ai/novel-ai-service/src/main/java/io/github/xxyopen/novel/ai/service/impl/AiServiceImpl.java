package io.github.xxyopen.novel.ai.service.impl;


import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.github.xxyopen.novel.ai.service.AiService;
import io.github.xxyopen.novel.common.constant.CacheConsts;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.lang.System;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;
    private static final String model_qwen = "qwen-plus";
    private static final String model_qwen_tts = "qwen-tts";
    private static final String model_cosyvoice = "cosyvoice-v2";
    private static final String model_wanx = "wanx2.1-t2i-turbo";
    @Override
    public String continueText(String text, Double length) {
        String prompt = "请续写以下文本，续写长度约为" + length.intValue() + "字：" + text;
        return generateWithFallback(prompt);
    }

    @Override
    public String expandText(String text, Double ratio) {
        String prompt = "请将以下文本扩写为原长度的" + ratio / 100 + "倍：" + text;
        return generateWithFallback(prompt);
    }

    @Override
    public String condenseText(String text, Double ratio) {
        String prompt = "请将以下文本缩写为原长度的" + 100 / ratio.intValue() + "分之一：" + text;
        return generateWithFallback(prompt);
    }

    @Override
    public String polishText(String text) {
        String prompt = "请润色优化以下文本，保持原意：" + text;
        return generateWithFallback(prompt);
    }

    private String generateWithFallback(String prompt) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(model_qwen)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = gen.call(param);
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (ApiException e) {
            // 捕获并处理 API 异常
            System.err.println("DashScope API 调用失败: " + e.getMessage());
            // 在这里可以添加备用逻辑或返回默认响应
            return "无法获取结果，请稍后重试。";
        } catch (Exception e) {
            // 处理其他通用异常
            System.err.println("发生未知错误: " + e.getMessage());
            return "内部错误，请联系技术支持。";
        }
    }
    @Cacheable(
            cacheManager = CacheConsts.REDIS_CACHE_MANAGER,
            value = CacheConsts.TTS_AUDIO_CACHE_NAME,
            key = "#root.methodName + ':' + T(org.springframework.util.DigestUtils).md5DigestAsHex(#text.getBytes()) + ':' + #voiceType"
    )
    @Override
    public String textToSpeech_qwen_tts(String text, String voiceType) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            // 构建语音合成参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model(model_qwen_tts)
                    .apiKey(dashScopeApiKey)
                    .text(text)
                    .voice(AudioParameters.Voice.valueOf(voiceType))
                    .build();
            // 调用API进行语音合成
            MultiModalConversationResult result = conv.call(param);
            System.out.println("\n音频合成成功，音频文件URL: " + result.getOutput().getAudio().getUrl());
            return result.getOutput().getAudio().getUrl();
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("处理语音合成时出错: " + e.getMessage());
            return null;
        }
    }


    //语音合成
    @Override
    public Flux<String> textToSpeech_qwen_tts_Flux(String text, String voiceType) {
        try {
            log.info("开始处理语音合成，文本: {}, 语音类型: {}", text, voiceType);
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model(model_qwen_tts)
                    .apiKey(dashScopeApiKey)
                    .text(text)
                    .voice(AudioParameters.Voice.valueOf(voiceType))
                    .build();

            Flowable<MultiModalConversationResult> result = conv.streamCall(param);

            // 直接返回带 Base64 前缀的音频数据
            return Flux.from(result.map(r -> " audio/wav;base64," + r.getOutput().getAudio().getData() + "\n\n"));


        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
            return Flux.empty();
        } catch (Exception e) {
            System.out.println("处理语音合成时出错: " + e.getMessage());
            return Flux.empty();
        }
    }

    @Override
    public String textToSpeech_cosyvoice(String text, String voiceType) {
        try{
            // 请求参数
            SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                    .apiKey("sk-232a5143cc26411cb706e4760a64f9d5")
                    .model(model_cosyvoice) // 模型
                    .voice(voiceType) // 音色
                    .build();
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
            ByteBuffer audio = synthesizer.call(text);
            File file = new File("logs/tts_output.wav");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(audio.array());
                System.out.println("\n音频文件已下载到本地: " + file.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        } catch (Exception e) {
            System.out.println("处理语音合成时出错: " + e.getMessage());
            return null;
        }
    }
    //图片生成
    @Override
    public String textToImage(String text) {
        try {
            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(model_wanx)
                    .prompt(text)
                    .n(1)
                    .size("1024*1024")
                    .build();
            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = imageSynthesis.call(param);
            return result.toString();
        } catch (Exception e) {
            System.out.println("文本生成图片时出错: " + e.getMessage());
            return null;
        }
    }
}
