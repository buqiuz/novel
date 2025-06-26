package io.github.xxyopen.novel.ai.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * AI 处理结果 DTO
 */
@Data
@Builder
public class AiResponse {

    /**
     * AI 生成的结果文本
     */
    private String result;

}

