package io.github.xxyopen.novel.resource.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-28 19:13:05
 */
@Data
@Builder
public class ImgVerifyCodeReqDto {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "验证码")
    private String code;
}
