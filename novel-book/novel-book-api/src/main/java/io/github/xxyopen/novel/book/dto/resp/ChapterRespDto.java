package io.github.xxyopen.novel.book.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-25 16:10:06
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterRespDto {

    @Schema(description = "章节名")
    private String chapterName;

    @Schema(description = "章节内容")
    private String chapterContent;

    @Schema(description = "是否VIP章节：0 免费，1 收费")
    private Integer isVip;
}
