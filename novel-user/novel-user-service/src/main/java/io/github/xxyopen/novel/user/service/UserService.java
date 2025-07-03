package io.github.xxyopen.novel.user.service;


import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.user.dao.entity.UserReadHistory;
import io.github.xxyopen.novel.user.dto.req.UserInfoUptReqDto;
import io.github.xxyopen.novel.user.dto.req.UserLoginReqDto;
import io.github.xxyopen.novel.user.dto.req.UserReadHistoryReqDto;
import io.github.xxyopen.novel.user.dto.req.UserRegisterReqDto;
import io.github.xxyopen.novel.user.dto.resp.*;

import java.util.List;

/**
 * 会员模块 服务类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册参数
     * @return JWT
     */
    RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return JWT + 昵称
     */
    RestResp<UserLoginRespDto> login(UserLoginReqDto dto);

    /**
     * 用户反馈
     *
     * @param userId  反馈用户ID
     * @param content 反馈内容
     * @return void
     */
    RestResp<Void> saveFeedback(Long userId, String content);

    /**
     * 用户信息修改
     *
     * @param dto 用户信息
     * @return void
     */
    RestResp<Void> updateUserInfo(UserInfoUptReqDto dto);

    /**
     * 用户反馈删除
     *
     * @param userId 用户ID
     * @param id     反馈ID
     * @return void
     */
    RestResp<Void> deleteFeedback(Long userId, Long id);

    /**
     * 查询书架状态接口
     *
     * @param userId 用户ID
     * @param bookId 小说ID
     * @return 0-不在书架 1-已在书架
     */
    RestResp<Integer> getBookshelfStatus(Long userId, String bookId);

    /**
     * 用户信息查询
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    RestResp<UserInfoRespDto> getUserInfo(Long userId);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds);


    /**
     * 删除用户
     * @param userId
     */
    RestResp<Void> delete(Long userId);

    /**
     * 存储用户阅读记录
     */
    RestResp<Void> saveUserReadHistory(UserReadHistoryReqDto dto);

    /**
     * 获取用户阅读记录
     */
    RestResp<PageRespDto<UserReadHistoryRespDto>> listUserReadHistory(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户书架
     */
    RestResp<PageRespDto<UserBookshelfRespDto>> listUserBookshelf(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 添加书籍到用户书架
     */
    RestResp<Void> addBookshelf(Long userId, Long bookId, Long preContentId);
    /**
     * 删除用户书架书籍
     */
    RestResp<Void> deleteBookshelf(Long userId, Long bookId);
    /**
     * 查看是否位于书架
     */
    RestResp<Boolean> isInBookshelf(Long userId, Long bookId);
}
