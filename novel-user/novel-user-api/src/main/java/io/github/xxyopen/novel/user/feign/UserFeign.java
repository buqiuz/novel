package io.github.xxyopen.novel.user.feign;

import io.github.xxyopen.novel.common.constant.ApiRouterConsts;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.user.dto.req.UserBookReqDto;
import io.github.xxyopen.novel.user.dto.req.UserReadHistoryReqDto;
import io.github.xxyopen.novel.user.dto.resp.UserInfoRespDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户微服务调用客户端
 *
 * @author xiongxiaoyang
 * @date 2023/3/29
 */
@Component
@FeignClient(value = "novel-user-service", fallback = UserFeign.UserFeignFallback.class)
public interface UserFeign {

    /**
     * 批量查询用户信息
     */
    @PostMapping(ApiRouterConsts.API_INNER_USER_URL_PREFIX + "/listUserInfoByIds")
    RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds);

    /**
     * 存储用户阅读记录
     */
    @PostMapping(ApiRouterConsts.API_INNER_USER_URL_PREFIX + "/saveUserReadHistory")
    RestResp<Boolean> saveUserReadHistory(UserReadHistoryReqDto dto);

    /**
     * 查询书籍是否位于书架中
     */
    @PostMapping(ApiRouterConsts.API_INNER_USER_URL_PREFIX + "/isBookInShelf")
    RestResp<Boolean> isBookInShelf(UserBookReqDto dto);

    @Component
    class UserFeignFallback implements UserFeign {

        @Override
        public RestResp<List<UserInfoRespDto>> listUserInfoByIds(List<Long> userIds) {

            return RestResp.ok(new ArrayList<>(0));

        }
        @Override
        public RestResp<Boolean> saveUserReadHistory(UserReadHistoryReqDto dto) {
            System.out.println("调用用户服务失败，返回默认值");
            return RestResp.ok(false);
        }
        @Override
        public RestResp<Boolean> isBookInShelf(UserBookReqDto dto) {
            System.out.println("调用用户服务失败，返回默认值");
            System.out.println(dto);
            return RestResp.ok(false);
        }
    }

}
