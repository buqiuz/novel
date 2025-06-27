package io.github.xxyopen.novel.author.manager.feign;

import io.github.xxyopen.novel.ai.dto.req.AiTextRequest;
import io.github.xxyopen.novel.ai.dto.resp.AiResponse;
import io.github.xxyopen.novel.ai.feign.AiServiceFeign;
import io.github.xxyopen.novel.common.resp.RestResp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <p>
 * AiFeignManager 用于管理 AI 相关的 Feign 客户端调用。
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-26 16:13:16
 */
@Component
@AllArgsConstructor
public class AiFeignManager {

    private final AiServiceFeign aiServiceFeign;

    /**
     * 调用 AI 接口生成续写文本
     */
    public RestResp<String> continueText(AiTextRequest request) {
        return aiServiceFeign.continueText(request.getText(), request.getLength());
    }

    /**
     * 调用 AI 接口扩展文本
     */
    public RestResp<String> expandText(AiTextRequest request) {
        return aiServiceFeign.expandText(request.getText(), request.getRatio());
    }

    /**
     * 调用 AI 接口浓缩文本
     */
    public RestResp<String> condenseText(AiTextRequest request) {
        return aiServiceFeign.condenseText(request.getText(), request.getRatio());
    }

    /**
     * 调用 AI 接口润色文本
     */
    public RestResp<String> polishText(AiTextRequest request) {
        return aiServiceFeign.polishText(request.getText());
    }

}

