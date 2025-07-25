package io.github.xxyopen.novel.book.service;


import io.github.xxyopen.novel.book.dao.entity.BookInfo;
import io.github.xxyopen.novel.book.dto.req.*;
import io.github.xxyopen.novel.book.dto.resp.*;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 小说模块 服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/14
 */
public interface BookService {

    /**
     * 小说点击榜查询
     *
     * @return 小说点击排行列表
     */
    RestResp<List<BookRankRespDto>> listVisitRankBooks();

    /**
     * 小说新书榜查询
     *
     * @return 小说新书排行列表
     */
    RestResp<List<BookRankRespDto>> listNewestRankBooks();

    /**
     * 小说更新榜查询
     *
     * @return 小说更新排行列表
     */
    RestResp<List<BookRankRespDto>> listUpdateRankBooks();

    /**
     * 小说信息查询
     *
     * @param bookId 小说ID
     * @return 小说信息
     */
    RestResp<BookInfoRespDto> getBookById(Long bookId);

    /**
     * 小说内容相关信息查询
     *
     * @param chapterId 章节ID
     * @return 内容相关联的信息
     */
    RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId);

    /**
     * 小说最新章节相关信息查询
     *
     * @param bookId 小说ID
     * @return 章节相关联的信息
     */
    RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId);

    /**
     * 小说推荐列表查询
     *
     * @param bookId 小说ID
     * @return 小说信息列表
     */
    RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId) throws NoSuchAlgorithmException;

    /**
     * 增加小说点击量
     *
     * @param bookId 小说ID
     * @return 成功状态
     */
    RestResp<Void> addVisitCount(Long bookId);

    /**
     * 获取上一章节ID
     *
     * @param chapterId 章节ID
     * @return 上一章节ID
     */
    RestResp<Long> getPreChapterId(Long chapterId);

    /**
     * 获取下一章节ID
     *
     * @param chapterId 章节ID
     * @return 下一章节ID
     */
    RestResp<Long> getNextChapterId(Long chapterId);

    /**
     * 小说章节列表查询
     *
     * @param bookId 小说ID
     * @return 小说章节列表
     */
    RestResp<List<BookChapterRespDto>> listChapters(Long bookId);

    /**
     * 小说分类列表查询
     *
     * @param workDirection 作品方向;0-男频 1-女频
     * @return 分类列表
     */
    RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection);

    /**
     * 发表评论
     *
     * @param dto 评论相关 DTO
     * @return void
     */
    RestResp<Void> saveComment(BookCommentReqDto dto);

    /**
     * 小说最新评论查询
     *
     * @param bookId 小说ID
     * @return 小说最新评论数据
     */
    RestResp<BookCommentRespDto> listNewestComments(Long bookId);

    /**
     * 删除评论
     *
     * @param dto 评论相关 DTO
     * @return void
     */
    RestResp<Void> deleteComment(BookCommentReqDto dto);

    /**
     * 修改评论
     *
     * @param dto 评论相关 DTO
     * @return void
     */
    RestResp<Void> updateComment(BookCommentReqDto dto);

    /**
     * 小说信息保存
     *
     * @param dto 小说信息
     * @return void
     */
    RestResp<Void> saveBook(BookAddReqDto dto);

    /**
     * 小说章节信息保存
     *
     * @param dto 章节信息
     * @return void
     */
    RestResp<Void> saveBookChapter(ChapterAddReqDto dto);

    /**
     * 查询作家发布小说列表
     *
     * @param dto 分页请求参数
     * @return 小说分页列表数据
     */
    RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(BookPageReqDto dto);

    /**
     * 查询小说发布章节列表
     *
     * @param dto    分页请求参数
     * @return 章节分页列表数据
     */
    RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(ChapterPageReqDto dto);

    /**
     * 查询下一批保存到 ES 中的小说列表
     *
     * @param maxBookId 已查询的最大小说ID
     * @return 小说列表
     */
    RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId);

    /**
     * 批量查询小说信息
     *
     * @param bookIds 小说ID列表
     * @return 小说信息列表
     */
    RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds);

    /**
     * 删除小说章节
     *
     * @param chapterId 章节ID
     * @return void
     */
    RestResp<Void> deleteBookChapter(Long chapterId);

    /**
     * 获取小说章节信息
     *
     * @param chapterId 章节ID
     * @return 小说章节信息
     */
    RestResp<ChapterRespDto> getBookChapter(Long chapterId);

    RestResp<Void> updateBookChapter(ChapterUpdateReqDto dto);

    /**
     * 删除小说
     *
     * @param bookId 小说ID
     * @return 操作结果
     */
    RestResp<Void> deleteBook(Long bookId);

    /**
     * 更新小说信息
     *
     * @param dto 小说更新信息
     * @return 操作结果
     */
    RestResp<Void> updateBook(BookUpdateReqDto dto);

    /**
     * 小说章节解锁
     * @param userId 用户ID
     * @param chapterId 章节ID
     * @return 操作结果
     */
    RestResp<Boolean> insertBookChapterUnlock(Long userId,Long chapterId);
    /**
     * 小说章节解锁信息查询
     * @param userId 用户ID
     * @param chapterId 章节ID
     * @return 操作结果
     */
    RestResp<Integer> getBookChapterUnlock(Long userId,Long bookId,Long chapterId);

    /**
     * 获取所有小说ID列表
     *
     * @return 所有小说ID列表
     */
    RestResp<List<Long>> listAllBookIds();

    /**
     * 查询所有小说信息列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页小说信息列表
     */
    RestResp<PageRespDto<BookInfo>> listAllBookInfos(Integer pageNum, Integer pageSize);

    /**
     * 查询用户评论列表
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页用户评论列表
     */
    RestResp<PageRespDto<UserCommentRespDto>> listUserComments(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 批量查询小说名
     */
    RestResp<Map<Long, String>> listBookNames(List<Long> bookIds);
    /**
     * 批量查询章节名
     */
    RestResp<Map<Long, String>> listChapterNames(List<Long> chapterIds);
    /**
     * 批量查询小说名、章节名、图片连接
     */
    RestResp<List<BookshelfInfoRespDto>> listBookChapterNamesAndPics(List<BookshelfInfoReqDto> Dto);

}
