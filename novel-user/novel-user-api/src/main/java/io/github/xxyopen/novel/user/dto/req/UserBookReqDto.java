package io.github.xxyopen.novel.user.dto.req;

import lombok.Builder;
import lombok.Data;

@Data
public class UserBookReqDto {
    private Long userId;
    private Long bookId;

    public UserBookReqDto(Long userId, Long bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }
}
