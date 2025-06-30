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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.ai.service.AiService;
import io.github.xxyopen.novel.common.constant.CacheConsts;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.lang.System;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executors;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;

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
            // 使用ObjectMapper转换为Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resultMap = mapper.convertValue(result.getOutput().getResults().get(0), Map.class);
            return (String) resultMap.get("url");
        } catch (Exception e) {
            System.out.println("文本生成图片时出错: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String pngToJpg(String url) {
        try {
            return saveAsJpgToImageDir(url);
        } catch (Exception e) {
            System.out.println("文本生成图片时出错: " + e.getMessage());
            return null;
        }
    }


    private String saveAsJpgToImageDir(String pngUrl) throws IOException {
        // 确保image目录存在
        File imageDir = new File("image");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        // 1. 下载PNG图片
        URL url = new URL(pngUrl);
        BufferedImage pngImage = ImageIO.read(url);

        if (pngImage == null) {
            throw new IOException("下载图片失败");
        }

        // 2. 创建JPG格式的BufferedImage
        BufferedImage jpgImage = new BufferedImage(
                pngImage.getWidth(),
                pngImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // 3. 绘制并转换格式
        jpgImage.createGraphics().drawImage(pngImage, 0, 0, Color.WHITE, null);

        // 4. 生成唯一文件名
        String fileName = "cover_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(imageDir, fileName);

        // 5. 保存为JPG
        if (!ImageIO.write(jpgImage, "jpg", outputFile)) {
            throw new IOException("无法保存为JPG格式");
        }
        // 6. 返回相对路径（前端拼接基础URL）
        return "/image/" + fileName;
    }


    public static byte[] addWavHeader(byte[] pcmData, int sampleRate, int channels, int bitsPerSample) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int dataLength = pcmData.length;

        ByteBuffer buffer = ByteBuffer.allocate(44 + dataLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + dataLength);
        buffer.put("WAVE".getBytes());
        buffer.put("fmt ".getBytes());
        buffer.putInt(16); // PCM
        buffer.putShort((short) 1); // PCM
        buffer.putShort((short) channels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort((short) blockAlign);
        buffer.putShort((short) bitsPerSample);
        buffer.put("data".getBytes());
        buffer.putInt(dataLength);
        buffer.put(pcmData);

        return buffer.array();
    }

    /**
     * 使用 Qwen 模型进行语音合成，并通过 SseEmitter 实时推送音频流
     *
     * @return SseEmitter
     */
    public SseEmitter textToSpeechQwenStream(String text,String voiceType) {
        SseEmitter emitter = new SseEmitter(0L); // 无超时限制

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                MultiModalConversation conv = new MultiModalConversation();
                MultiModalConversationParam param = MultiModalConversationParam.builder()
                        .model("qwen-tts")
                        .text(text)
                        .apiKey("sk-232a5143cc26411cb706e4760a64f9d5")
                        .voice(AudioParameters.Voice.valueOf(voiceType))
                        .build();

                Flowable<MultiModalConversationResult> result = conv.streamCall(param);
                result.blockingForEach(r -> {
                    try {
                        String base64Data = r.getOutput().getAudio().getData();
                        // 添加 WAV 头后再 Base64 编码并发送
                        byte[] audioBytes = Base64.getDecoder().decode(base64Data);
                        if (audioBytes.length == 0) {
                            log.warn("收到空音频数据，跳过");
                            return;
                        }
                        byte[] wavData = addWavHeader(audioBytes, 24000, 1, 16); // 根据模型实际采样率
                        String base64Wav = Base64.getEncoder().encodeToString(wavData);

                        emitter.send(SseEmitter.event()
                                .name("audioChunk")
                                .data(base64Wav));

                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}
