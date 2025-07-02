package io.github.xxyopen.novel.book.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserCommentRespDto {
    private Long user_id;
    private Long book_id;
    private String book_name;
    private String comment_content;
    private LocalDateTime createTime;

}
