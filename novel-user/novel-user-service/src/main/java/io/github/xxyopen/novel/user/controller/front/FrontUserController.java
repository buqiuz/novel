package io.github.xxyopen.novel.user.controller.front;

import io.github.xxyopen.novel.book.dto.req.BookCommentReqDto;
import io.github.xxyopen.novel.common.auth.UserHolder;
import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.constant.SystemConfigConsts;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.user.dao.entity.UserReadHistory;
import io.github.xxyopen.novel.user.dto.req.UserInfoUptReqDto;
import io.github.xxyopen.novel.user.dto.req.UserLoginReqDto;
import io.github.xxyopen.novel.user.dto.req.UserRegisterReqDto;
import io.github.xxyopen.novel.user.dto.resp.*;
import io.github.xxyopen.novel.user.manager.feign.BookFeignManager;
import io.github.xxyopen.novel.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 前台门户-会员模块 API 控制器
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Tag(name = "UserController", description = "前台门户-会员模块")
@SecurityRequirement(name = SystemConfigConsts.HTTP_AUTH_HEADER_NAME)
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_USER_URL_PREFIX)
@RequiredArgsConstructor
public class FrontUserController {

    private final UserService userService;

    private final BookFeignManager bookFeignManager;

    /**
     * 用户注册接口
     */
    @Operation(summary = "用户注册接口")
    @PostMapping("register")
    public RestResp<UserRegisterRespDto> register(@Valid @RequestBody UserRegisterReqDto dto) {
        return userService.register(dto);
    }

    /**
     * 用户登录接口
     */
    @Operation(summary = "用户登录接口")
    @PostMapping("login")
    public RestResp<UserLoginRespDto> login(@Valid @RequestBody UserLoginReqDto dto) {
        return userService.login(dto);
    }

    /**
     * 用户删除接口
     */
    @Operation(summary = "用户删除接口")
    @DeleteMapping("delete")
    public RestResp<Void> delete() {
        return userService.delete(UserHolder.getUserId());
    }


    /**
     * 用户信息查询接口
     */
    @Operation(summary = "用户信息查询接口")
    @GetMapping
    public RestResp<UserInfoRespDto> getUserInfo() {
        return userService.getUserInfo(UserHolder.getUserId());
    }

    /**
     * 用户信息修改接口
     */
    @Operation(summary = "用户信息修改接口")
    @PutMapping
    public RestResp<Void> updateUserInfo(@Valid @RequestBody UserInfoUptReqDto dto) {
        dto.setUserId(UserHolder.getUserId());
        return userService.updateUserInfo(dto);
    }

    /**
     * 用户反馈提交接口
     */
    @Operation(summary = "用户反馈提交接口")
    @PostMapping("feedback")
    public RestResp<Void> submitFeedback(@RequestBody String content) {
        return userService.saveFeedback(UserHolder.getUserId(), content);
    }

    /**
     * 用户反馈删除接口
     */
    @Operation(summary = "用户反馈删除接口")
    @DeleteMapping("feedback/{id}")
    public RestResp<Void> deleteFeedback(@Parameter(description = "反馈ID") @PathVariable Long id) {
        return userService.deleteFeedback(UserHolder.getUserId(), id);
    }

    /**
     * 发表评论接口
     */
    @Operation(summary = "发表评论接口")
    @PostMapping("comment")
    public RestResp<Void> comment(@Valid @RequestBody BookCommentReqDto dto) {
        return bookFeignManager.publishComment(dto);
    }

    /**
     * 修改评论接口
     */
    @Operation(summary = "修改评论接口")
    @PutMapping("comment/{id}")
    public RestResp<Void> updateComment(@Parameter(description = "评论ID") @PathVariable Long id,
        String content) {
        BookCommentReqDto dto = new BookCommentReqDto();
        dto.setUserId(UserHolder.getUserId());
        dto.setCommentId(id);
        dto.setCommentContent(content);
        return bookFeignManager.updateComment(dto);
    }

    /**
     * 删除评论接口
     */
    @Operation(summary = "删除评论接口")
    @DeleteMapping("comment/{id}")
    public RestResp<Void> deleteComment(@Parameter(description = "评论ID") @PathVariable Long id) {
        BookCommentReqDto dto = new BookCommentReqDto();
        dto.setUserId(UserHolder.getUserId());
        dto.setCommentId(id);
        return bookFeignManager.deleteComment(dto);
    }

    /**
     * 查询书架状态接口 0-不在书架 1-已在书架
     */
    @Operation(summary = "查询书架状态接口")
    @GetMapping("bookshelf_status")
    public RestResp<Integer> getBookshelfStatus(@Parameter(description = "小说ID") String bookId) {
        return userService.getBookshelfStatus(UserHolder.getUserId(), bookId);
    }

    /**
     * 查询用户阅读历史接口
     */
    @Operation(summary = "查询用户阅读历史接口")
    @GetMapping("read_history")
    public RestResp<PageRespDto<UserReadHistoryRespDto>> listUserReadHistory(
            @Parameter(description = "页码") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam Integer pageSize) {
        return userService.listUserReadHistory(UserHolder.getUserId(), pageNum, pageSize);
    }

    /**
     * 查询用户书架接口
     */
    @Operation(summary = "查询用户书架接口")
    @GetMapping("bookshelf")
    public RestResp<PageRespDto<UserBookshelfRespDto>> listUserBookshelf(
            @Parameter(description = "页码") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam Integer pageSize) {
        return userService.listUserBookshelf(UserHolder.getUserId(), pageNum, pageSize);
    }

    /**
     * 添加用户书架接口
     */
    @Operation(summary = "添加用户书架接口")
    @PostMapping("addBookshelf")
    public RestResp<Void> addBookshelf(
            @Parameter(description = "小说ID") @RequestParam Long bookId,
            @Parameter(description = "小说最新章节ID") @RequestParam Long preContentId ){
        return userService.addBookshelf(UserHolder.getUserId(), bookId, preContentId);
    }

    /**
     * 删除用户书架接口
     */
    @Operation(summary = "删除用户书架接口")
    @DeleteMapping("deleteBookshelf")
    public RestResp<Void> deleteBookshelf(
            @Parameter(description = "小说ID") @RequestParam Long bookId ){
        return userService.deleteBookshelf(UserHolder.getUserId(), bookId);
    }
    /**
     * 判断书籍是否在书架中
     */
    @Operation(summary = "判断书籍是否在书架中")
    @GetMapping("isInBookshelf")
    public RestResp<Boolean> isInBookshelf(
            @Parameter(description = "小说ID") @RequestParam Long bookId ){
        return userService.isInBookshelf(UserHolder.getUserId(), bookId);
    }
}
