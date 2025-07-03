package io.github.xxyopen.novel.book.dto.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookshelfInfoRespDto {
    private String book_name;
    private String preContent_name;
    private String pic_url;
}
