package io.github.xxyopen.novel.search.listener;

import io.github.xxyopen.novel.book.dao.entity.BookInfo;
import io.github.xxyopen.novel.book.dao.mapper.BookInfoMapper;
import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;
import io.github.xxyopen.novel.common.constant.AmqpConsts;
import io.github.xxyopen.novel.search.service.BookEsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-25 09:30:20
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookChangeMqListener {

    private final BookInfoMapper bookInfoMapper;
    private final BookEsSyncService bookEsSyncService;

    public class BookConverter {

        public static BookEsRespDto toEsDto(BookInfo bookInfo) {
            return BookEsRespDto.builder()
                    .id(bookInfo.getId())
                    .workDirection(bookInfo.getWorkDirection())
                    .categoryId(bookInfo.getCategoryId())
                    .categoryName(bookInfo.getCategoryName())
                    .bookName(bookInfo.getBookName())
                    .authorId(bookInfo.getAuthorId())
                    .authorName(bookInfo.getAuthorName())
                    .bookDesc(bookInfo.getBookDesc())
                    .score(bookInfo.getScore())
                    .bookStatus(bookInfo.getBookStatus())
                    .visitCount(bookInfo.getVisitCount())
                    .wordCount(bookInfo.getWordCount())
                    .commentCount(bookInfo.getCommentCount())
                    .lastChapterId(bookInfo.getLastChapterId())
                    .lastChapterName(bookInfo.getLastChapterName())
                    .lastChapterUpdateTime(bookInfo.getLastChapterUpdateTime() != null
                            ? bookInfo.getLastChapterUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            : null)
                    .isVip(bookInfo.getIsVip())
                    .build();
        }
    }

    @RabbitListener(queues = AmqpConsts.BookChangeMq.QUEUE_ES_UPDATE)
    public void onBookChange(Long bookId) {
        try {
            BookInfo bookInfo = bookInfoMapper.selectById(bookId);
            if (bookInfo != null) {
                // 有书籍：转换并同步到 ES
                BookEsRespDto esDto = BookConverter.toEsDto(bookInfo);
                bookEsSyncService.syncBook(esDto);
                log.info("已同步书籍 {} 到 ES", bookId);
            } else {
                // 无此书：从 ES 删除
                bookEsSyncService.removeBook(bookId);
                log.info("已从 ES 删除书籍 {}", bookId);
            }
        } catch (Exception e) {
            log.error("处理书籍变更消息失败，书籍ID={}，原因={}", bookId, e.getMessage(), e);
        }
    }

}
