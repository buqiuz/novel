package io.github.xxyopen.novel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.xxyopen.novel.book.feign.BookFeign;
import io.github.xxyopen.novel.common.auth.JwtUtils;
import io.github.xxyopen.novel.common.constant.CommonConsts;
import io.github.xxyopen.novel.common.constant.DatabaseConsts;
import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.constant.SystemConfigConsts;
import io.github.xxyopen.novel.common.resp.PageRespDto;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.config.exception.BusinessException;
import io.github.xxyopen.novel.user.dao.entity.*;
import io.github.xxyopen.novel.user.dao.mapper.*;
import io.github.xxyopen.novel.user.dto.req.UserInfoUptReqDto;
import io.github.xxyopen.novel.user.dto.req.UserLoginReqDto;
import io.github.xxyopen.novel.user.dto.req.UserReadHistoryReqDto;
import io.github.xxyopen.novel.user.dto.req.UserRegisterReqDto;
import io.github.xxyopen.novel.user.dto.resp.UserInfoRespDto;
import io.github.xxyopen.novel.user.dto.resp.UserLoginRespDto;
import io.github.xxyopen.novel.user.dto.resp.UserReadHistoryRespDto;
import io.github.xxyopen.novel.user.dto.resp.UserRegisterRespDto;
import io.github.xxyopen.novel.user.manager.redis.VerifyCodeManager;
import io.github.xxyopen.novel.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 会员模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/17
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserInfoMapper userInfoMapper;

    private final VerifyCodeManager verifyCodeManager;

    private final UserFeedbackMapper userFeedbackMapper;

    private final UserBookshelfMapper userBookshelfMapper;

    private final UserWalletMapper userWalletMapper;

    private final UserReadHistoryMapper userReadHistoryMapper;

    private final BookFeign bookFeign;

    @Override
    public RestResp<UserRegisterRespDto> register(UserRegisterReqDto dto) {

        // 校验短信验证码
        if (!verifyCodeManager.smsVerifyCodeOk(dto.getSessionId(), dto.getSmsCode())) {
            throw new BusinessException(ErrorCodeEnum.USER_VERIFY_CODE_ERROR);
        }

        // 注册用户
        UserInfo userInfo = new UserInfo();
        userInfo.setPassword(
                DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        userInfo.setUsername(dto.getUsername());
        userInfo.setNickName(dto.getUsername());
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfo.setSalt("0");
        userInfoMapper.insert(userInfo);

        //初始化用户钱包
        UserWallet userWallet = new UserWallet();
        userWallet.setUserId(userInfo.getId());
        userWallet.setGoldBalance(0L); // 初始金币余额设为0
        userWallet.setCreatedTime(LocalDateTime.now());
        userWallet.setUpdatedTime(LocalDateTime.now());
        userWalletMapper.insert(userWallet);

        // 删除验证码
        verifyCodeManager.removeSmsVerifyCode(dto.getSessionId());

        // 返回结果
        return RestResp.ok(
                UserRegisterRespDto.builder()
                        .token(JwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
                        .uid(userInfo.getId())
                        .build()
        );
    }


    @Override
    public RestResp<UserLoginRespDto> login(UserLoginReqDto dto) {
        // 查询用户信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserInfoTable.COLUMN_USERNAME, dto.getUsername())
            .last(DatabaseConsts.SqlEnum.LIMIT_1.getSql());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }

        // 判断密码是否正确
        if (!Objects.equals(userInfo.getPassword()
            , DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)))) {
            // 密码错误
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        // 登录成功，生成JWT并返回
        return RestResp.ok(UserLoginRespDto.builder()
            .token(JwtUtils.generateToken(userInfo.getId(), SystemConfigConsts.NOVEL_FRONT_KEY))
            .uid(userInfo.getId())
            .nickName(userInfo.getNickName()).build());
    }

    @Override
    public RestResp<Void> saveFeedback(Long userId, String content) {
        UserFeedback userFeedback = new UserFeedback();
        userFeedback.setUserId(userId);
        userFeedback.setContent(content);
        userFeedback.setCreateTime(LocalDateTime.now());
        userFeedback.setUpdateTime(LocalDateTime.now());
        userFeedbackMapper.insert(userFeedback);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> updateUserInfo(UserInfoUptReqDto dto) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(dto.getUserId());
        userInfo.setNickName(dto.getNickName());
        userInfo.setUserPhoto(dto.getUserPhoto());
        userInfo.setUserSex(dto.getUserSex());
        userInfoMapper.updateById(userInfo);
        return RestResp.ok();
    }

    @Override
    public RestResp<Void> deleteFeedback(Long userId, Long id) {
        QueryWrapper<UserFeedback> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.CommonColumnEnum.ID.getName(), id)
            .eq(DatabaseConsts.UserFeedBackTable.COLUMN_USER_ID, userId);
        userFeedbackMapper.delete(queryWrapper);
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getBookshelfStatus(Long userId, String bookId) {
        QueryWrapper<UserBookshelf> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(DatabaseConsts.UserBookshelfTable.COLUMN_USER_ID, userId)
            .eq(DatabaseConsts.UserBookshelfTable.COLUMN_BOOK_ID, bookId);
        return RestResp.ok(
            userBookshelfMapper.selectCount(queryWrapper) > 0
                ? CommonConsts.YES
                : CommonConsts.NO
        );
    }

    @Override
    public RestResp<UserInfoRespDto> getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return RestResp.ok(UserInfoRespDto.builder()
                .id(userInfo.getId())
            .nickName(userInfo.getNickName())
            .userSex(userInfo.getUserSex())
            .userPhoto(userInfo.getUserPhoto())
            .build());
    }

    @Override
    public RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(DatabaseConsts.CommonColumnEnum.ID.getName(), userIds);
        return RestResp.ok(
            userInfoMapper.selectList(queryWrapper).stream().map(v -> UserInfoRespDto.builder()
                .id(v.getId())
                .username(v.getUsername())
                .userPhoto(v.getUserPhoto())
                .build()).collect(Collectors.toList()));
    }

    @Override
    public RestResp<Void> delete(Long userId) {
        int result = userInfoMapper.deleteById(userId);

        if (result > 0) {
            return RestResp.ok(); // 删除成功
        } else {
            throw new BusinessException(ErrorCodeEnum.USER_DELETE_ERROR);
        }
    }

    @Override
    public RestResp<Void> saveUserReadHistory(UserReadHistoryReqDto dto) {
        QueryWrapper<UserReadHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", dto.getUserId()).eq("book_id", dto.getBookId());
        UserReadHistory existing = userReadHistoryMapper.selectOne(queryWrapper);
        if(existing ==  null){
            System.out.println("保存");
            UserReadHistory userReadHistory = new UserReadHistory();
            userReadHistory.setUserId(dto.getUserId());
            userReadHistory.setBookId(dto.getBookId());
            userReadHistory.setPreContentId(dto.getPreContentId());
            userReadHistory.setCreateTime(LocalDateTime.now());
            userReadHistory.setUpdateTime(LocalDateTime.now());
            userReadHistoryMapper.insert(userReadHistory);
            System.out.println("保存成功");
        }
        else{
            System.out.println("更新");
            existing.setPreContentId(dto.getPreContentId());
            existing.setUpdateTime(LocalDateTime.now());
            userReadHistoryMapper.updateById(existing);
            System.out.println("更新成功");
        }
        return RestResp.ok();
    }
    @Override
    public RestResp<PageRespDto<UserReadHistoryRespDto>> listUserReadHistory(Long userId, Integer pageNum, Integer pageSize){
        QueryWrapper<UserReadHistory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("id");
        Page<UserReadHistory> page = new Page<>(pageNum, pageSize);
        Page<UserReadHistory> result = userReadHistoryMapper.selectPage(page, queryWrapper);
        RestResp<Map<Long, String>> res1 = bookFeign.listBookNames(result.getRecords().stream().map(UserReadHistory::getBookId).toList());
        Map<Long, String> bookNames = res1.getData();
        RestResp<Map<Long, String>> res2 = bookFeign.listChapterNames(result.getRecords().stream().map(UserReadHistory::getPreContentId).toList());
        Map<Long, String> chapterNames = res2.getData();
        // 将结果转换为UserReadHistoryRespDTO
        List<UserReadHistoryRespDto> dtoList = new ArrayList<>();
        for (UserReadHistory history : result.getRecords()) {
            UserReadHistoryRespDto dto = UserReadHistoryRespDto.builder()
                    .bookId(history.getBookId())
                    .preContentId(history.getPreContentId())
                    .bookName(bookNames.getOrDefault(history.getBookId(), "未知小说"))
                    .preChapterName(chapterNames.getOrDefault(history.getPreContentId(), "未知章节"))
                    .updateTime(history.getUpdateTime())
                    .build();
            dtoList.add(dto);
        }
        return RestResp.ok(PageRespDto.of(pageNum, pageSize, result.getTotal(), dtoList));
    }
}
