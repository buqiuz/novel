package io.github.xxyopen.novel.book.dto.req;

import lombok.Data;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-25 19:58:16
 */
@Data
public class ChapterUpdateReqDto {
    private Long chapterId;
    private Long bookId;
    private String chapterName;
    private String chapterContent;
    private Integer isVip;
    private Integer authorId; // 可选：用于权限校验
}
