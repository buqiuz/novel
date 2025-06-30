package io.github.xxyopen.novel.book.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@TableName("chapter_unlocks")
public class ChapterUnlock implements Serializable {
    private Long userId;
    private Long chapterId;
    private LocalDateTime unlockedAt;
    private Long spentTokens;
}
