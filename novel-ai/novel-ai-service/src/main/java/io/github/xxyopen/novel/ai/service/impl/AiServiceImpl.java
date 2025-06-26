package io.github.xxyopen.novel.ai.service.impl;


import io.github.xxyopen.novel.ai.service.AiService;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    @Autowired
    public AiServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

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
            Prompt p = new Prompt(new UserMessage(prompt));
            ChatResponse response = chatClient.call(p);
            if (response != null && response.getResult() != null) {
                return response.getResult().getOutput().getContent();
            } else {
                return "AI 暂时无法完成该请求，请稍后再试。";
            }
        } catch (Exception e) {
            return "AI 调用失败，请检查服务状态。";
        }
    }
}
