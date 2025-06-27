package io.github.xxyopen.novel.ai.service.impl;


import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.github.xxyopen.novel.ai.service.AiService;
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

@Service
public class AiServiceImpl implements AiService {

    private static final String model_qwen = "qwen-plus";
    private static final String model_qwen_tts = "qwen-tts";
    private static final String model_cosyvoice = "cosyvoice-v2";
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
            String protocol = "http"; // 或 "websocket"
            String baseUrl = "https://dashscope.aliyuncs.com/api/v1";
            Generation gen = new Generation(protocol, baseUrl);
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(prompt)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                    .apiKey("sk-232a5143cc26411cb706e4760a64f9d5")
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
    @Override
    public String textToSpeech_qwen_tts(String text, String voiceType) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            // 构建语音合成参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model(model_qwen_tts)
                    .apiKey("sk-232a5143cc26411cb706e4760a64f9d5")
                    .text(text)
                    .voice(AudioParameters.Voice.valueOf(voiceType))
                    .build();
            // 调用API进行语音合成
            MultiModalConversationResult result = conv.call(param);
//            // 下载音频文件到本地
//            try (InputStream in = new URL(audioUrl).openStream();
//                 FileOutputStream out = new FileOutputStream("logs/tts_output.wav")) {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//                System.out.println("\n音频文件已下载到本地: downloaded_audio.wav");
//            }
            return result.getOutput().getAudio().getUrl();
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("处理语音合成时出错: " + e.getMessage());
            return null;
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
}
