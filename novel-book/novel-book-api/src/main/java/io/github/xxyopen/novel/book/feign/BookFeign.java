package io.github.xxyopen.novel.book.feign;

import io.github.xxyopen.novel.book.dto.req.*;
import io.github.xxyopen.novel.book.dto.resp.BookChapterRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;
import io.github.xxyopen.novel.book.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.book.dto.resp.ChapterRespDto;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小说微服务调用客户端
 *
 * @author xiongxiaoyang
 * @date 2023/3/29
 */
@Component
@FeignClient(value = "novel-book-service", fallback = BookFeign.BookFeignFallback.class)
public interface BookFeign {

    /**
     * 查询下一批保存到 ES 中的小说列表
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listNextEsBooks")
    RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId);

    /**
     * 批量查询小说信息
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listBookInfoByIds")
    RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds);

    /**
     * 发表评论
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishComment")
    RestResp<Void> publishComment(BookCommentReqDto dto);

    /**
     * 修改评论
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/updateComment")
    RestResp<Void> updateComment(BookCommentReqDto dto);

    /**
     * 删除评论接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/deleteComment")
    RestResp<Void> deleteComment(@RequestBody BookCommentReqDto dto);

    /**
     * 小说发布接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishBook")
    RestResp<Void> publishBook(BookAddReqDto dto);

    /**
     * 小说发布列表查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listPublishBooks")
    RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(BookPageReqDto dto);

    /**
     * 小说章节发布接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/publishBookChapter")
    RestResp<Void> publishBookChapter(ChapterAddReqDto dto);

    /**
     * 小说章节发布列表查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listPublishBookChapters")
    RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(ChapterPageReqDto dto);

    /**
     * 删除小说章节
     */
    @DeleteMapping (ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/deleteBookChapter")
    RestResp<Void> deleteBookChapter(Long chapterId);

    /**
     * 获取小说章节详情
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/getBookChapter")
    RestResp<ChapterRespDto> getBookChapter(Long chapterId);

    /**
     * 更新小说章节
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/updateBookChapter")
    RestResp<Void> updateBookChapter(@RequestBody ChapterUpdateReqDto dto);

    /**
     * 删除小说
     */
    @DeleteMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/deleteBook")
    RestResp<Void> deleteBook(Long bookId);

    /**
     * 小说更新接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/updateBook")
    RestResp<Void> updateBook(@RequestBody BookUpdateReqDto dto);

    /**
     * 获取所有小说ID
     */
    @GetMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listAllBookIds")
    RestResp<List<Long>> listAllBookIds();

    /**
     * 章节解锁接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/insertBookChapterUnlock")
    RestResp<Boolean> insertBookChapterUnlock(@RequestBody ChapterUnlockReqDto dto);

    /**
     * 小说名批量查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listBookNames")
    RestResp<Map<Long, String>> listBookNames(@RequestBody List<Long> bookIds);

    /**
     * 章节名批量查询接口
     */
    @PostMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX + "/listChapterNames")
    RestResp<Map<Long, String>> listChapterNames(@RequestBody List<Long> chapterIds);

    @Component
    class BookFeignFallback implements BookFeign {

        @Override
        public RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId) {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds) {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<Void> publishComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> updateComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> deleteComment(BookCommentReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> publishBook(BookAddReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(BookPageReqDto dto) {
            return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, new ArrayList<>(0)));
        }

        @Override
        public RestResp<Void> publishBookChapter(ChapterAddReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(ChapterPageReqDto dto) {
            return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), 0, new ArrayList<>(0)));
        }

        @Override
        public RestResp<Void> deleteBookChapter(Long chapterId) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<ChapterRespDto> getBookChapter(Long chapterId) {
            // 返回结构完整、字段为空的 ChapterRespDto
            ChapterRespDto dto = new ChapterRespDto();
            dto.setChapterName("");
            dto.setChapterContent("");
            dto.setIsVip(0); // 默认设置为免费

            return RestResp.ok(dto);
        }

        @Override
        public RestResp<Void> updateBookChapter(ChapterUpdateReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> deleteBook(Long bookId) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }

        @Override
        public RestResp<Void> updateBook(BookUpdateReqDto dto) {
            return RestResp.fail(ErrorCodeEnum.THIRD_SERVICE_ERROR);
        }
        @Override
        public RestResp<List<Long>> listAllBookIds() {
            return RestResp.ok(new ArrayList<>(0));
        }

        @Override
        public RestResp<Boolean> insertBookChapterUnlock(ChapterUnlockReqDto dto) {
            return RestResp.ok( false);
        }

        @Override
        public RestResp<Map<Long, String>> listBookNames(List<Long> bookIds) {
            return RestResp.ok(new HashMap<>(0));
        }

        @Override
        public RestResp<Map<Long, String>> listChapterNames(List<Long> chapterIds) {
            return RestResp.ok(new HashMap<>(0));
        }
    }

}
