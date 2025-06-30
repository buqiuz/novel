package io.github.xxyopen.novel.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xxyopen.novel.book.dao.entity.*;
import io.github.xxyopen.novel.book.dao.mapper.*;
import io.github.xxyopen.novel.book.dto.req.*;
import io.github.xxyopen.novel.book.dto.resp.*;
import io.github.xxyopen.novel.book.manager.cache.*;
import io.github.xxyopen.novel.book.manager.feign.UserFeignManager;
import io.github.xxyopen.novel.book.manager.mq.AmqpMsgManager;
import io.github.xxyopen.novel.book.service.BookService;
import io.github.xxyopen.novel.common.auth.UserHolder;
import io.github.xxyopen.novel.common.constant.DatabaseConsts;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.config.annotation.Key;
import io.github.xxyopen.novel.config.annotation.Lock;
import io.github.xxyopen.novel.user.dto.resp.UserInfoRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小说模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookCategoryCacheManager bookCategoryCacheManager;

    private final BookRankCacheManager bookRankCacheManager;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final BookInfoMapper bookInfoMapper;

    private final BookChapterMapper bookChapterMapper;

    private final BookContentMapper bookContentMapper;

    private final BookCommentMapper bookCommentMapper;

    private final AmqpMsgManager amqpMsgManager;

    private final UserFeignManager userFeignManager;

    private final ChapterUnlockMapper chapterUnlockMapper;

    private static final Integer REC_BOOK_COUNT = 4;

    @Override
    public RestResp<List<BookRankRespDto>> listVisitRankBooks() {
        return RestResp.ok(bookRankCacheManager.listVisitRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listNewestRankBooks() {
        return RestResp.ok(bookRankCacheManager.listNewestRankBooks());
    }

    @Override
    public RestResp<List<BookRankRespDto>> listUpdateRankBooks() {
        return RestResp.ok(bookRankCacheManager.listUpdateRankBooks());
    }

    @Override
    public RestResp<BookInfoRespDto> getBookById(Long bookId) {
        return RestResp.ok(bookInfoCacheManager.getBookInfo(bookId));
    }

    @Override
    public RestResp<BookChapterAboutRespDto> getLastChapterAbout(Long bookId) {
        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);

        // 查询最新章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(
            bookInfo.getLastChapterId());

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(bookInfo.getLastChapterId());

        // 查询章节总数
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        Long chapterTotal = bookChapterMapper.selectCount(chapterQueryWrapper);

        // 组装数据并返回
        return RestResp.ok(BookChapterAboutRespDto.builder()
            .chapterInfo(bookChapter)
            .chapterTotal(chapterTotal)
            .contentSummary(content.substring(0, 30))
            .build());
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listRecBooks(Long bookId)
        throws NoSuchAlgorithmException {
        Long categoryId = bookInfoCacheManager.getBookInfo(bookId).getCategoryId();
        List<Long> lastUpdateIdList = bookInfoCacheManager.getLastUpdateIdList(categoryId);
        List<BookInfoRespDto> respDtoList = new ArrayList<>();
        List<Integer> recIdIndexList = new ArrayList<>();
        int count = 0;
        Random rand = SecureRandom.getInstanceStrong();
        while (count < REC_BOOK_COUNT) {
            int recIdIndex = rand.nextInt(lastUpdateIdList.size());
            if (!recIdIndexList.contains(recIdIndex)) {
                recIdIndexList.add(recIdIndex);
                bookId = lastUpdateIdList.get(recIdIndex);
                BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
                respDtoList.add(bookInfo);
                count++;
            }
        }
        return RestResp.ok(respDtoList);
    }

    @Override
    public RestResp<Void> addVisitCount(Long bookId) {
        bookInfoMapper.addVisitCount(bookId);
        return RestResp.ok();
    }

    @Override
    public RestResp<Long> getPreChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询上一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .lt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
            Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                .map(BookChapter::getId)
                .orElse(null)
        );
    }

    @Override
    public RestResp<Long> getNextChapterId(Long chapterId) {
        // 查询小说ID 和 章节号
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        Long bookId = chapter.getBookId();
        Integer chapterNum = chapter.getChapterNum();

        // 查询下一章信息并返回章节ID
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .gt(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM, chapterNum)
            .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        return RestResp.ok(
            Optional.ofNullable(bookChapterMapper.selectOne(queryWrapper))
                .map(BookChapter::getId)
                .orElse(null)
        );
    }

    @Override
    public RestResp<List<BookChapterRespDto>> listChapters(Long bookId) {
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
            .orderByAsc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        return RestResp.ok(bookChapterMapper.selectList(queryWrapper).stream()
            .map(v -> BookChapterRespDto.builder()
                .id(v.getId())
                .chapterName(v.getChapterName())
                .isVip(v.getIsVip())
                .build()).toList());
    }

    @Override
    public RestResp<List<BookCategoryRespDto>> listCategory(Integer workDirection) {
        return RestResp.ok(bookCategoryCacheManager.listCategory(workDirection));
    }

    @Lock(prefix = "userComment")
    @Override
    public RestResp<Void> saveComment(
        @Key(expr = "#{userId + '::' + bookId}") BookCommentReqDto dto) {
        // 校验用户是否已发表评论
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId())
            .eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, dto.getBookId());
        if (bookCommentMapper.selectCount(queryWrapper) > 0) {
            // 用户已发表评论
            return RestResp.fail(ErrorCodeEnum.USER_COMMENTED);
        }
        BookComment bookComment = new BookComment();
        bookComment.setBookId(dto.getBookId());
        bookComment.setUserId(dto.getUserId());
        bookComment.setCommentContent(dto.getCommentContent());
        bookComment.setCreateTime(LocalDateTime.now());
        bookComment.setUpdateTime(LocalDateTime.now());
        bookCommentMapper.insert(bookComment);
        return RestResp.ok();
    }

    @Override
    public RestResp<BookCommentRespDto> listNewestComments(Long bookId) {
        // 查询评论总数
        QueryWrapper<BookComment> commentCountQueryWrapper = new QueryWrapper<>();
        commentCountQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        Long commentTotal = bookCommentMapper.selectCount(commentCountQueryWrapper);
        BookCommentRespDto bookCommentRespDto = BookCommentRespDto.builder()
            .commentTotal(commentTotal).build();
        if (commentTotal > 0) {

            // 查询最新的评论列表
            QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId)
                .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName())
                .last(DatabaseConsts.SqlEnum.LIMIT_5.getSql());
            List<BookComment> bookComments = bookCommentMapper.selectList(commentQueryWrapper);

            // 查询评论用户信息，并设置需要返回的评论用户名
            List<Long> userIds = bookComments.stream().map(BookComment::getUserId).toList();
            List<UserInfoRespDto> userInfos = userFeignManager.listUserInfoByIds(userIds);
            Map<Long, UserInfoRespDto> userInfoMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfoRespDto::getId, Function.identity()));
            List<BookCommentRespDto.CommentInfo> commentInfos = bookComments.stream()
                .map(v -> BookCommentRespDto.CommentInfo.builder()
                    .id(v.getId())
                    .commentUserId(v.getUserId())
                    .commentUser(userInfoMap.get(v.getUserId()).getUsername())
                    .commentUserPhoto(userInfoMap.get(v.getUserId()).getUserPhoto())
                    .commentContent(v.getCommentContent())
                    .commentTime(v.getCreateTime()).build()).toList();
            bookCommentRespDto.setComments(commentInfos);
        } else {
            bookCommentRespDto.setComments(Collections.emptyList());
        }
        return RestResp.ok(bookCommentRespDto);
    }

    @Override
    public RestResp<Void> deleteComment(BookCommentReqDto dto) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), dto.getCommentId())
            .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId());
        bookCommentMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateComment(BookCommentReqDto dto) {
        QueryWrapper<BookComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), dto.getCommentId())
            .eq(DatabaseConsts.BookCommentTable.COLUMN_USER_ID, dto.getUserId());
        BookComment bookComment = new BookComment();
        bookComment.setCommentContent(dto.getCommentContent());
        bookCommentMapper.update(bookComment, queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> saveBook(BookAddReqDto dto) {
        // 校验小说名是否已存在
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.COLUMN_BOOK_NAME, dto.getBookName());
        if (bookInfoMapper.selectCount(queryWrapper) > 0) {
            return RestResp.fail(ErrorCodeEnum.AUTHOR_BOOK_NAME_EXIST);
        }
        BookInfo bookInfo = new BookInfo();
        // 设置作家信息
        bookInfo.setAuthorId(dto.getAuthorId());
        bookInfo.setAuthorName(dto.getPenName());
        // 设置其他信息
        bookInfo.setWorkDirection(dto.getWorkDirection());
        bookInfo.setCategoryId(dto.getCategoryId());
        bookInfo.setCategoryName(dto.getCategoryName());
        bookInfo.setBookName(dto.getBookName());
        bookInfo.setPicUrl(dto.getPicUrl());
        bookInfo.setBookDesc(dto.getBookDesc());
        bookInfo.setIsVip(dto.getIsVip());
        bookInfo.setScore(0);
        bookInfo.setCreateTime(LocalDateTime.now());
        bookInfo.setUpdateTime(LocalDateTime.now());
        // 保存小说信息
        bookInfoMapper.insert(bookInfo);
        log.info("新插入小说ID：{}", bookInfo.getId());

        // ✅ 发送 MQ 消息（事务提交后发送）
        amqpMsgManager.sendBookChangeMsg(bookInfo.getId());

        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> saveBookChapter(ChapterAddReqDto dto) {
        // 校验该作品是否属于当前作家
        BookInfo bookInfo = bookInfoMapper.selectById(dto.getBookId());
        if (!Objects.equals(bookInfo.getAuthorId(), dto.getAuthorId())) {
            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
        }
        // 1) 保存章节相关信息到小说章节表
        //  a) 查询最新章节号
        int chapterNum = 0;
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter bookChapter = bookChapterMapper.selectOne(chapterQueryWrapper);
        if (Objects.nonNull(bookChapter)) {
            chapterNum = bookChapter.getChapterNum() + 1;
        }
        //  b) 设置章节相关信息并保存
        BookChapter newBookChapter = new BookChapter();
        newBookChapter.setBookId(dto.getBookId());
        newBookChapter.setChapterName(dto.getChapterName());
        newBookChapter.setChapterNum(chapterNum);
        newBookChapter.setWordCount(dto.getChapterContent().length());
        newBookChapter.setIsVip(dto.getIsVip());
        newBookChapter.setCreateTime(LocalDateTime.now());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.insert(newBookChapter);

        // 2) 保存章节内容到小说内容表
        BookContent bookContent = new BookContent();
        bookContent.setContent(dto.getChapterContent());
        bookContent.setChapterId(newBookChapter.getId());
        bookContent.setCreateTime(LocalDateTime.now());
        bookContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.insert(bookContent);

        // 3) 更新小说表最新章节信息和小说总字数信息
        //  a) 更新小说表关于最新章节的信息
        BookInfo newBookInfo = new BookInfo();
        newBookInfo.setId(dto.getBookId());
        newBookInfo.setLastChapterId(newBookChapter.getId());
        newBookInfo.setLastChapterName(newBookChapter.getChapterName());
        newBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        newBookInfo.setWordCount(bookInfo.getWordCount() + newBookChapter.getWordCount());
        newBookChapter.setUpdateTime(LocalDateTime.now());
        bookInfoMapper.updateById(newBookInfo);
        //  b) 清除小说信息缓存
        bookInfoCacheManager.evictBookInfoCache(dto.getBookId());
        //  c) 发送小说信息更新的 MQ 消息
        amqpMsgManager.sendBookChangeMsg(dto.getBookId());
        return RestResp.ok();
    }

    @Override
    public RestResp<PageRespDto<BookInfoRespDto>> listAuthorBooks(BookPageReqDto dto) {
        IPage<BookInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookTable.AUTHOR_ID, dto.getAuthorId())
            .orderByDesc(DatabaseConsts.CommonColumnEnum.CREATE_TIME.getName());
        IPage<BookInfo> bookInfoPage = bookInfoMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
            bookInfoPage.getRecords().stream().map(v -> BookInfoRespDto.builder()
                .id(v.getId())
                .bookName(v.getBookName())
                .picUrl(v.getPicUrl())
                .categoryName(v.getCategoryName())
                .wordCount(v.getWordCount())
                .visitCount(v.getVisitCount())
                .updateTime(v.getUpdateTime())
                .build()).toList()));
    }

    @Override
    public RestResp<PageRespDto<BookChapterRespDto>> listBookChapters(ChapterPageReqDto dto) {
        IPage<BookChapter> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        QueryWrapper<BookChapter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, dto.getBookId())
            .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM);
        IPage<BookChapter> bookChapterPage = bookChapterMapper.selectPage(page, queryWrapper);
        return RestResp.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(),
            bookChapterPage.getRecords().stream().map(v -> BookChapterRespDto.builder()
                .id(v.getId())
                .chapterName(v.getChapterName())
                .chapterUpdateTime(v.getUpdateTime())
                .isVip(v.getIsVip())
                .build()).toList()));
    }

    @Override
    public RestResp<List<BookEsRespDto>> listNextEsBooks(Long maxBookId) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.clear();
        queryWrapper
            .orderByAsc(DatabaseConsts.CommonColumnEnum.ID.getName())
            .gt(DatabaseConsts.CommonColumnEnum.ID.getName(), maxBookId)
            .gt(DatabaseConsts.BookTable.COLUMN_WORD_COUNT, 0)
            .last(DatabaseConsts.SqlEnum.LIMIT_30.getSql());
        return RestResp.ok(bookInfoMapper.selectList(queryWrapper).stream().map(bookInfo -> BookEsRespDto.builder()
            .id(bookInfo.getId())
            .categoryId(bookInfo.getCategoryId())
            .categoryName(bookInfo.getCategoryName())
            .bookDesc(bookInfo.getBookDesc())
            .bookName(bookInfo.getBookName())
            .authorId(bookInfo.getAuthorId())
            .authorName(bookInfo.getAuthorName())
            .bookStatus(bookInfo.getBookStatus())
            .commentCount(bookInfo.getCommentCount())
            .isVip(bookInfo.getIsVip())
            .score(bookInfo.getScore())
            .visitCount(bookInfo.getVisitCount())
            .wordCount(bookInfo.getWordCount())
            .workDirection(bookInfo.getWorkDirection())
            .lastChapterId(bookInfo.getLastChapterId())
            .lastChapterName(bookInfo.getLastChapterName())
            .lastChapterUpdateTime(bookInfo.getLastChapterUpdateTime()
                .toInstant(ZoneOffset.ofHours(8)).toEpochMilli())
            .build()).collect(Collectors.toList()));
    }

    @Override
    public RestResp<List<BookInfoRespDto>> listBookInfoByIds(List<Long> bookIds) {
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), bookIds);
        return RestResp.ok(
            bookInfoMapper.selectList(queryWrapper).stream().map(v -> BookInfoRespDto.builder()
                .id(v.getId())
                .bookName(v.getBookName())
                .authorName(v.getAuthorName())
                .picUrl(v.getPicUrl())
                .bookDesc(v.getBookDesc())
                .build()).collect(Collectors.toList()));
    }

    @Override
    public RestResp<BookContentAboutRespDto> getBookContentAbout(Long chapterId) {
        log.debug("userId:{}", UserHolder.getUserId());
        // 查询章节信息
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(chapterId);

        // 查询章节内容
        String content = bookContentCacheManager.getBookContent(chapterId);

        // 查询小说信息
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookChapter.getBookId());

        // 组装数据并返回
        return RestResp.ok(BookContentAboutRespDto.builder()
            .bookInfo(bookInfo)
            .chapterInfo(bookChapter)
            .bookContent(content)
            .build());
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> deleteBookChapter(Long chapterId) {
        // 1. 查询章节信息
        BookChapter bookChapter = bookChapterMapper.selectById(chapterId);
        if (bookChapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_CHAPTER_NOT_EXIST);
        }

        Long bookId = bookChapter.getBookId();

        // 2. 删除章节内容
        QueryWrapper<BookContent> contentWrapper = new QueryWrapper<>();
        contentWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);
        bookContentMapper.delete(contentWrapper);

        // 3. 删除章节记录
        bookChapterMapper.deleteById(chapterId);

        // 4. 查询该小说的剩余章节中最新的那一章（可能被删除了最后一章）
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId)
                .orderByDesc(DatabaseConsts.BookChapterTable.COLUMN_CHAPTER_NUM)
                .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        BookChapter latestChapter = bookChapterMapper.selectOne(chapterQueryWrapper);

        // 5. 更新小说信息（字数、最新章节）
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        int updatedWordCount = Math.max(0, bookInfo.getWordCount() - bookChapter.getWordCount());

        BookInfo updateBookInfo = new BookInfo();
        updateBookInfo.setId(bookId);
        updateBookInfo.setWordCount(updatedWordCount);
        if (latestChapter != null) {
            updateBookInfo.setLastChapterId(latestChapter.getId());
            updateBookInfo.setLastChapterName(latestChapter.getChapterName());
            updateBookInfo.setLastChapterUpdateTime(latestChapter.getUpdateTime());
        } else {
            // 所有章节都删完了，清空章节相关字段
            updateBookInfo.setLastChapterId(null);
            updateBookInfo.setLastChapterName(null);
            updateBookInfo.setLastChapterUpdateTime(null);
        }
        bookInfoMapper.updateById(updateBookInfo);

        // 6. 清除缓存
        bookInfoCacheManager.evictBookInfoCache(bookId);

        // 7. 发送 MQ 消息
        amqpMsgManager.sendBookChangeMsg(bookId);

        return RestResp.ok();
    }
    @Override
    public RestResp<ChapterRespDto> getBookChapter(Long chapterId) {
        BookChapterRespDto bookChapter = bookChapterCacheManager.getChapter(chapterId);
        String content = bookContentCacheManager.getBookContent(chapterId);
        return RestResp.ok(ChapterRespDto.builder()
            .chapterName(bookChapter.getChapterName())
            .isVip(bookChapter.getIsVip())
            .chapterContent(content)
            .build());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> updateBookChapter(ChapterUpdateReqDto dto) {
        Long chapterId = dto.getChapterId();

        // 1. 校验章节是否存在
        BookChapter bookChapter = bookChapterMapper.selectById(chapterId);
        if (bookChapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_CHAPTER_NOT_EXIST);
        }

//        // 2. 校验是否为作者本人操作（如需要）
        BookInfo bookInfo = bookInfoMapper.selectById(bookChapter.getBookId());
//        if (!Objects.equals(bookInfo.getAuthorId(), dto.getAuthorId())) {
//            return RestResp.fail(ErrorCodeEnum.USER_UN_AUTH);
//        }

        // 3. 更新章节内容表
        UpdateWrapper<BookContent> contentWrapper = new UpdateWrapper<>();
        contentWrapper.eq(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterId);

        BookContent updateContent = new BookContent();
        updateContent.setChapterId(chapterId);
        updateContent.setContent(dto.getChapterContent());
        updateContent.setUpdateTime(LocalDateTime.now());
        bookContentMapper.update(updateContent, contentWrapper);

        // 4. 重新计算字数
        int newWordCount = dto.getChapterContent() != null ? dto.getChapterContent().length() : 0;
        int wordDiff = newWordCount - bookChapter.getWordCount();

        // 5. 更新章节信息
        BookChapter updateChapter = new BookChapter();
        updateChapter.setId(chapterId);
        updateChapter.setChapterName(dto.getChapterName());
        updateChapter.setIsVip(dto.getIsVip());
        updateChapter.setWordCount(newWordCount);
        updateChapter.setUpdateTime(LocalDateTime.now());
        bookChapterMapper.updateById(updateChapter);

        // 6. 更新小说总字数
        int updatedWordCount = Math.max(0, bookInfo.getWordCount() + wordDiff);
        BookInfo updateBookInfo = new BookInfo();
        updateBookInfo.setId(bookInfo.getId());
        updateBookInfo.setWordCount(updatedWordCount);
        updateBookInfo.setUpdateTime(LocalDateTime.now());

        // 如果更新的是最新章节，也更新最新章节信息
        if (Objects.equals(bookInfo.getLastChapterId(), chapterId)) {
            updateBookInfo.setLastChapterName(dto.getChapterName());
            updateBookInfo.setLastChapterUpdateTime(LocalDateTime.now());
        }

        bookInfoMapper.updateById(updateBookInfo);

        // 7. 清除相关缓存（章节、内容、小说信息）
        bookInfoCacheManager.evictBookInfoCache(bookInfo.getId());
        bookChapterCacheManager.evictBookChapterCache(chapterId);
        bookContentCacheManager.evictBookContentCache(chapterId);


        // 8. MQ 通知
        amqpMsgManager.sendBookChangeMsg(bookInfo.getId());

        return RestResp.ok();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RestResp<Void> deleteBook(Long bookId) {
        // 1. 查询小说是否存在
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_EXIST);
        }

        // 2. 删除小说内容和章节
        // 2.1 查询所有章节ID
        QueryWrapper<BookChapter> chapterQueryWrapper = new QueryWrapper<>();
        chapterQueryWrapper.eq(DatabaseConsts.BookChapterTable.COLUMN_BOOK_ID, bookId);
        List<BookChapter> bookChapters = bookChapterMapper.selectList(chapterQueryWrapper);
        List<Long> chapterIds = bookChapters.stream().map(BookChapter::getId).toList();

        // 2.2 删除章节内容
        if (!chapterIds.isEmpty()) {
            QueryWrapper<BookContent> contentQueryWrapper = new QueryWrapper<>();
            contentQueryWrapper.in(DatabaseConsts.BookContentTable.COLUMN_CHAPTER_ID, chapterIds);
            bookContentMapper.delete(contentQueryWrapper);
        }

        // 2.3 删除所有章节
        bookChapterMapper.delete(chapterQueryWrapper);

        // 3. 删除小说评论
        QueryWrapper<BookComment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq(DatabaseConsts.BookCommentTable.COLUMN_BOOK_ID, bookId);
        bookCommentMapper.delete(commentQueryWrapper);

        // 4. 删除小说信息
        bookInfoMapper.deleteById(bookId);

        // 5. 清除相关缓存
        bookInfoCacheManager.evictBookInfoCache(bookId);

        // 6. 发送MQ消息，通知其他服务小说已被删除
        amqpMsgManager.sendBookChangeMsg(bookId);

        return RestResp.ok();
    }

    @Override
    public RestResp<Boolean> insertBookChapterUnlock(Long userId, Long chapterId){
        ChapterUnlock chapterUnlock = new ChapterUnlock();
        chapterUnlock.setUserId(userId);
        chapterUnlock.setChapterId(chapterId);
        chapterUnlock.setUnlockedAt(LocalDateTime.now());
        chapterUnlock.setSpentTokens(50L);
        QueryWrapper<ChapterUnlock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("chapter_id", chapterId);
        Long count = chapterUnlockMapper.selectCount(queryWrapper);
        if(count > 0){
            return RestResp.ok(Boolean.FALSE);
        }
        else{
            int rowsAffected = chapterUnlockMapper.insert(chapterUnlock);
            return rowsAffected > 0 ? RestResp.ok(Boolean.TRUE): RestResp.ok(Boolean.FALSE);
        }
    }

    @Override
    public RestResp<Boolean> getBookChapterUnlock(Long userId, Long chapterId){
        QueryWrapper<ChapterUnlock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("chapter_id", chapterId);
        Long count = chapterUnlockMapper.selectCount(queryWrapper);
        return count > 0 ? RestResp.ok(Boolean.TRUE) : RestResp.ok(Boolean.FALSE);
    }
}
