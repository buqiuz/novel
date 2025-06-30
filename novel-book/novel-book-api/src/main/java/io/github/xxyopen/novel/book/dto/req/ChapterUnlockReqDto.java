package io.github.xxyopen.novel.book.dto.req;

import lombok.Data;

@Data
public class ChapterUnlockReqDto {
    private Long userId;
    private Long chapterId;
}
