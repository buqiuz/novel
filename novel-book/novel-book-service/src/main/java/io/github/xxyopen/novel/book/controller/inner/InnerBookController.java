package io.github.xxyopen.novel.book.controller.inner;

import io.github.xxyopen.novel.book.dto.req.*;
import io.github.xxyopen.novel.book.dto.resp.*;
import io.github.xxyopen.novel.book.service.BookService;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.github.xxyopen.novel.book.dto.resp.BookshelfInfoRespDto;
import java.util.List;
import java.util.Map;

/**
 * 小说微服务内部调用接口
 *
 * @author xiongxiaoyang
 * @date 2023/3/29
 */
@Tag(name = "InnerBookController", description = "内部调用-小说模块")
@RestController
@RequestMapping(ApiRouterConsts.API_INNER_BOOK_URL_PREFIX)
@RequiredArgsConstructor
public class InnerBookController {

    private final BookService bookService;

    /**
     * 查询下一批保存到 ES 中的小说列表
     */
    @Operation(summary = "查询下一批保存到 ES 中的小说列表")
    @PostMapping("listNextEsBooks")
    RestResp<List<BookEsRespDto>> listNextEsBooks(@Parameter(description = "已查询的最大小说ID") @RequestBody Long maxBookId) {
        return bookService.listNextEsBooks(maxBookId);
    }

    /**
     * 批量查询小说信息
     */
    @Operation(summary = "批量查询小说信息")
    @PostMapping("listBookInfoByIds")
    RestResp<List<BookInfoRespDto>> listBookInfoByIds(@RequestBody List<Long> bookIds) {
        return bookService.listBookInfoByIds(bookIds);
    }

    /**
     * 发表评论接口
     */
    @Operation(summary = "发表评论接口")
    @PostMapping("publishComment")
    public RestResp<Void> publishComment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookService.saveComment(dto);
    }

    /**
     * 修改评论接口
     */
    @Operation(summary = "修改评论接口")
    @PostMapping("updateComment")
    public RestResp<Void> updateComment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookService.updateComment(dto);
    }

    /**
     * 删除评论接口
     */
    @Operation(summary = "删除评论接口")
    @PostMapping("deleteComment")
    public RestResp<Void> deleteComment(@RequestBody BookCommentReqDto dto) {
        return bookService.deleteComment(dto);
    }

    /**
     * 小说发布接口
     */
    @Operation(summary = "小说发布接口")
    @PostMapping("publishBook")
    public RestResp<Void> publishBook(@Valid @RequestBody BookAddReqDto dto) {
        return bookService.saveBook(dto);
    }

    /**
     * 小说发布列表查询接口
     */
    @Operation(summary = "小说发布列表查询接口")
    @PostMapping("listPublishBooks")
    public RestResp<PageRespDto<BookInfoRespDto>> listPublishBooks(@RequestBody BookPageReqDto dto) {
        return bookService.listAuthorBooks(dto);
    }

    /**
     * 小说章节发布接口
     */
    @Operation(summary = "小说章节发布接口")
    @PostMapping("publishBookChapter")
    public RestResp<Void> publishBookChapter(@Valid @RequestBody ChapterAddReqDto dto) {
        return bookService.saveBookChapter(dto);
    }

    /**
     * 小说章节发布列表查询接口
     */
    @Operation(summary = "小说章节发布列表查询接口")
    @PostMapping("listPublishBookChapters")
    public RestResp<PageRespDto<BookChapterRespDto>> listPublishBookChapters(@RequestBody ChapterPageReqDto dto) {
        return bookService.listBookChapters(dto);
    }
    /**
     * 删除小说章节
     */
    @Operation(summary = "删除小说章节")
    @DeleteMapping ("deleteBookChapter")
    public RestResp<Void> deleteBookChapter(@Parameter(description = "小说章节ID") @RequestBody Long chapterId) {
        return bookService.deleteBookChapter(chapterId);
    }
    /**
     * 获取小说章节详情
     */
    @Operation(summary = "获取小说章节详情")
    @PostMapping("getBookChapter")
    public RestResp<ChapterRespDto> getBookChapter(@Parameter(description = "小说章节ID") @RequestBody  Long chapterId) {
        return bookService.getBookChapter(chapterId);
    }
    /**
     * 更新小说章节
     */
    @Operation(summary = "更新小说章节")
    @PostMapping("updateBookChapter")
    public RestResp<Void> updateBookChapter(@RequestBody ChapterUpdateReqDto dto) {
        return bookService.updateBookChapter(dto);
    }

    /**
     * 删除小说
     */
    @Operation(summary = "删除小说")
    @DeleteMapping("deleteBook")
    public RestResp<Void> deleteBook(@Parameter(description = "小说ID") @RequestBody Long bookId) {
        return bookService.deleteBook(bookId);
    }

    /**
     * 更新小说信息
     */
    @Operation(summary = "更新小说信息")
    @CrossOrigin(origins = "*")
    @PostMapping("updateBook")
    public RestResp<Void> updateBook(@Valid @RequestBody BookUpdateReqDto dto) {
        return bookService.updateBook(dto);
    }

    /**
     * 获取所有小说ID
     */
    @Operation(summary = "获取所有小说ID")
    @GetMapping("listAllBookIds")
    public RestResp<List<Long>> listAllBookIds() {
        return bookService.listAllBookIds();
    }

    /**
     * 小说章节解锁接口
     */
    @Operation(summary = "小说章节解锁")
    @PostMapping("insertBookChapterUnlock")
    public RestResp<Boolean> insertBookChapterUnlock(@RequestBody @Valid ChapterUnlockReqDto dto) {
        return bookService.insertBookChapterUnlock(dto.getUserId(),dto.getChapterId());
    }

    /**
     * 小说名批量查询接口
     */
    @Operation(summary = "小说名批量查询接口")
    @PostMapping("listBookNames")
    public RestResp<Map<Long, String>> listBookNames(@RequestBody List<Long> bookIds) {
        return bookService.listBookNames(bookIds);
    }
    /**
     * 小说章节名批量查询接口
     */
    @Operation(summary = "小说章节名批量查询接口")
    @PostMapping("listChapterNames")
    public RestResp<Map<Long, String>> listChapterNames(@RequestBody List<Long> chapterIds) {
        return bookService.listChapterNames(chapterIds);
    }
    /**
     * 书架信息批量查询接口
     */
    @Operation(summary = "书架信息批量查询接口")
    @PostMapping("listBookShelfInfos")
    public RestResp<List<BookshelfInfoRespDto>> listBookShelfInfos(@RequestBody List<BookshelfInfoReqDto> Dto) {
        return bookService.listBookChapterNamesAndPics(Dto);
    }
}
