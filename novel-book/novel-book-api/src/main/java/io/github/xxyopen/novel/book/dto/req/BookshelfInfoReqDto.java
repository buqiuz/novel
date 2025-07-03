package io.github.xxyopen.novel.book.dto.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookshelfInfoReqDto {
    private Long bookId;
    private Long preContentId;
}
