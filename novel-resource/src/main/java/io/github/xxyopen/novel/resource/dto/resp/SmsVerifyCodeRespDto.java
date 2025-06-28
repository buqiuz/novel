package io.github.xxyopen.novel.resource.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-28 15:18:08
 */
@Data
@Builder
public class SmsVerifyCodeRespDto {
    /**
     * 会话 ID，用于后续验证码校验
     */
    private String sessionId;
}

