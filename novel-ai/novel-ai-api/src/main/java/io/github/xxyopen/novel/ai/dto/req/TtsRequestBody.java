package io.github.xxyopen.novel.ai.dto.req;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-29 20:29:05
 */

import lombok.Data;

/**
 * TTS 请求体
 */
@Data
public class TtsRequestBody {
    private String text;
    private String voiceType;
}