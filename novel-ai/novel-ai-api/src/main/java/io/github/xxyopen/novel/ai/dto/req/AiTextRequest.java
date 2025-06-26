package io.github.xxyopen.novel.ai.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 文本处理 请求DTO
 */
@Data
public class AiTextRequest {

    /**
     * 待处理文本
     */
    @Schema(description = "待处理文本", required = true)
    @NotBlank(message = "文本内容不能为空！")
    private String text;

    /**
     * 扩写/缩写比例（可选）<br>
     * 示例：120 表示扩写为原长度的 1.2 倍；80 表示缩写为原长度的 0.8 倍
     */
    @Schema(description = "扩写/缩写比例（可选）", example = "120")
    private Double ratio;

    /**
     * 续写长度（可选）<br>
     * 示例：500 表示续写约 500 字
     */
    @Schema(description = "续写长度（可选）", example = "500")
    private Double length;

}

