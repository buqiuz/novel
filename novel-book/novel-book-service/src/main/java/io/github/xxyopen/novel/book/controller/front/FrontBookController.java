package io.github.xxyopen.novel.book.controller.front;

import io.github.xxyopen.novel.book.dao.entity.BookInfo;
import io.github.xxyopen.novel.book.dto.resp.*;
import io.github.xxyopen.novel.book.service.BookService;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 前台门户-小说模块 API 控制器
 *
 * @author xiongxiaoyang
 * @date 2022/5/14
 */
@Tag(name = "FrontBookController", description = "前台门户-小说模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_BOOK_URL_PREFIX)
@RequiredArgsConstructor
public class FrontBookController {

    private final BookService bookService;

    /**
     * 小说分类列表查询接口
     */
    @Operation(summary = "小说分类列表查询接口")
    @GetMapping("category/list")
    public RestResp<List<BookCategoryRespDto>> listCategory(
        @Parameter(description = "作品方向", required = true) Integer workDirection) {
        return bookService.listCategory(workDirection);
    }

    /**
     * 小说信息查询接口
     */
    @Operation(summary = "小说信息查询接口")
    @GetMapping("{id}")
    public RestResp<BookInfoRespDto> getBookById(
        @Parameter(description = "小说 ID") @PathVariable("id") Long bookId) {
        return bookService.getBookById(bookId);
    }

    /**
     * 增加小说点击量接口
     */
    @Operation(summary = "增加小说点击量接口")
    @PostMapping("visit")
    public RestResp<Void> addVisitCount(@Parameter(description = "小说ID") Long bookId) {
        return bookService.addVisitCount(bookId);
    }

    /**
     * 小说最新章节相关信息查询接口
     */
    @Operation(summary = "小说最新章节相关信息查询接口")
    @GetMapping("last_chapter/about")
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(
        @Parameter(description = "小说ID") Long bookId) {
        return bookService.getLastChapterAbout(bookId);
    }

    /**
     * 小说推荐列表查询接口
     */
    @Operation(summary = "小说推荐列表查询接口")
    @GetMapping("rec_list")
    public RestResp<List<BookInfoRespDto>> listRecBooks(
        @Parameter(description = "小说ID") Long bookId) throws NoSuchAlgorithmException {
        return bookService.listRecBooks(bookId);
    }

    /**
     * 小说章节列表查询接口
     */
    @Operation(summary = "小说章节列表查询接口")
    @GetMapping("chapter/list")
    public RestResp<List<BookChapterRespDto>> listChapters(
        @Parameter(description = "小说ID") Long bookId) {
        return bookService.listChapters(bookId);
    }

    /**
     * 小说内容相关信息查询接口
     */
    @Operation(summary = "小说内容相关信息查询接口")
    @GetMapping("content/{chapterId}")
    public RestResp<BookContentAboutRespDto> getBookContentAbout(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getBookContentAbout(chapterId);
    }

    /**
     * 获取上一章节ID接口
     */
    @Operation(summary = "获取上一章节ID接口")
    @GetMapping("pre_chapter_id/{chapterId}")
    public RestResp<Long> getPreChapterId(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getPreChapterId(chapterId);
    }

    /**
     * 获取下一章节ID接口
     */
    @Operation(summary = "获取下一章节ID接口")
    @GetMapping("next_chapter_id/{chapterId}")
    public RestResp<Long> getNextChapterId(
        @Parameter(description = "章节ID") @PathVariable("chapterId") Long chapterId) {
        return bookService.getNextChapterId(chapterId);
    }

    /**
     * 小说点击榜查询接口
     */
    @Operation(summary = "小说点击榜查询接口")
    @GetMapping("visit_rank")
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return bookService.listVisitRankBooks();
    }

    /**
     * 小说新书榜查询接口
     */
    @Operation(summary = "小说新书榜查询接口")
    @GetMapping("newest_rank")
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return bookService.listNewestRankBooks();
    }

    /**
     * 小说更新榜查询接口
     */
    @Operation(summary = "小说更新榜查询接口")
    @GetMapping("update_rank")
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return bookService.listUpdateRankBooks();
    }

    /**
     * 小说最新评论查询接口
     */
    @Operation(summary = "小说最新评论查询接口")
    @GetMapping("comment/newest_list")
    public RestResp<BookCommentRespDto> listNewestComments(
        @Parameter(description = "小说ID") Long bookId) {
        return bookService.listNewestComments(bookId);
    }

    /**
     * 小说章节解锁接口
     */
    @Operation(summary = "小说章节解锁接口")
    @PostMapping("chapter/unlock")
    public RestResp<Boolean> insertBookChapterUnlock(
            @Parameter(description = "用户ID") Long userId,
            @Parameter(description = "章节ID") Long chapterId ) {
        return bookService.insertBookChapterUnlock(userId, chapterId);
    }

    /**
     * 小说章节解锁信息查询接口
     */
    @Operation(summary = "小说章节解锁信息查询接口")
    @GetMapping("chapter/unlockInfo")
    public RestResp<Integer> getBookChapterUnlock(
            @Parameter(description = "用户ID") Long userId,
            @Parameter(description = "书籍ID") Long bookId,
            @Parameter(description = "章节ID") Long chapterId ) {
        return bookService.getBookChapterUnlock(userId,bookId, chapterId);
    }

    /**
     * 查询所有小说信息接口
     */
    @Operation(summary = "查询所有小说信息接口")
    @GetMapping("listAll")
    public RestResp<PageRespDto<BookInfo>> listAllBookInfos(
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return bookService.listAllBookInfos(pageNum, pageSize);
    }

    /**
     * 查询书评信息接口
     */
    @Operation(summary = "查询书评信息接口")
    @GetMapping("comment/user_list")
    public RestResp<PageRespDto<UserCommentRespDto>> listUserComments(
        @Parameter(description = "用户ID") Long userId,
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return bookService.listUserComments(userId, pageNum, pageSize);
    }
}
