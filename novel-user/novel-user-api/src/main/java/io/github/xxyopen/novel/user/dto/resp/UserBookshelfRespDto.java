package io.github.xxyopen.novel.user.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserBookshelfRespDto {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 小说ID
     */
    private Long bookId;

    /**
     * 小说封面
     */
    private String pic_url;

    /**
     * 小说名
     */
    private String bookName;

    /**
     * 上一次阅读的章节内容表ID
     */
    private Long preContentId;

    /**
     * 上一次阅读的章节名
     */
    private String preChapterName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
