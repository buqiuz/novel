package io.github.xxyopen.novel.home.service.impl;

import io.github.xxyopen.novel.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.common.resp.RestResp;
import io.github.xxyopen.novel.config.exception.BusinessException;
import io.github.xxyopen.novel.home.dao.entity.HomeBook;
import io.github.xxyopen.novel.home.dao.mapper.HomeBookMapper;
import io.github.xxyopen.novel.home.dao.mapper.HomeFriendLinkMapper;
import io.github.xxyopen.novel.home.dto.resp.HomeBookRespDto;
import io.github.xxyopen.novel.home.dto.resp.HomeFriendLinkRespDto;
import io.github.xxyopen.novel.home.manager.cache.FriendLinkCacheManager;
import io.github.xxyopen.novel.home.manager.cache.HomeBookCacheManager;
import io.github.xxyopen.novel.home.manager.feign.BookFeignManager;
import io.github.xxyopen.novel.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 首页模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2022/5/13
 */
@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final HomeBookCacheManager homeBookCacheManager;

    private final FriendLinkCacheManager friendLinkCacheManager;

    private final HomeBookMapper homeBookMapper;

    private final BookFeignManager bookFeignManager;


    @Override
    public RestResp<List<HomeBookRespDto>> listHomeBooks() {
        List<HomeBookRespDto> list = homeBookCacheManager.listHomeBooks();
        if (CollectionUtils.isEmpty(list)) {
            homeBookCacheManager.evictCache();
        }
        return RestResp.ok(list);
    }

    @Override
    public RestResp<List<HomeFriendLinkRespDto>> listHomeFriendLinks() {
        return RestResp.ok(friendLinkCacheManager.listFriendLinks());
    }

    @Override
    public RestResp<String> updateHomeBooks() {
        try {
            // 1. 清空home_book表中的现有数据
            homeBookMapper.delete(null);

            // 2. 获取所有小说ID
            List<Long> allBookIds = bookFeignManager.listAllBookIds();
            if (CollectionUtils.isEmpty(allBookIds)) {
                throw new BusinessException(ErrorCodeEnum.THIRD_SERVICE_ERROR);
            }

            // 3. 随机选择小说并插入数据
            // 假设各个类型的小说数量
            Map<Integer, Integer> typeCountMap = new HashMap<>();
            typeCountMap.put(0, 5);  // 轮播图 5本
            typeCountMap.put(1, 10); // 顶部栏 10本
            typeCountMap.put(2, 8);  // 本周强推 8本
            typeCountMap.put(3, 15); // 热门推荐 12本
            typeCountMap.put(4, 15); // 精品推荐 15本

            // 打乱小说ID顺序
            Collections.shuffle(allBookIds);

            List<HomeBook> homeBooks = new ArrayList<>();
            int currentIndex = 0;
            LocalDateTime now = LocalDateTime.now();

            // 为每种类型分配小说
            for (Map.Entry<Integer, Integer> entry : typeCountMap.entrySet()) {
                Integer type = entry.getKey();
                Integer count = entry.getValue();

                // 确保有足够的小说
                if (currentIndex + count > allBookIds.size()) {
                    count = allBookIds.size() - currentIndex;
                    if (count <= 0) {
                        break;
                    }
                }

                // 为当前类型添加小说
                for (int i = 0; i < count; i++) {
                    HomeBook homeBook = new HomeBook();
                    homeBook.setType(type);
                    homeBook.setSort(i + 1); // 从1开始排序
                    homeBook.setBookId(allBookIds.get(currentIndex++));
                    homeBook.setCreateTime(now);
                    homeBook.setUpdateTime(now);
                    homeBooks.add(homeBook);
                }
            }

            // 批量插入数据
            if (!homeBooks.isEmpty()) {
                for (HomeBook homeBook : homeBooks) {
                    homeBookMapper.insert(homeBook);
                }
            }

            // 4. 清除缓存
            homeBookCacheManager.evictCache();

            return RestResp.ok("首页小说推荐数据更新成功");
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.UPDATE_HOME_BOOK_ERROR);
        }
    }
}
