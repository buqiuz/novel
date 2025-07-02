package io.github.xxyopen.novel.book.manager.cache;

import io.github.xxyopen.novel.common.constant.CacheConsts;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;


/**
 * 首页推荐小说 缓存管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@RequiredArgsConstructor
public class HomeBookCacheManager {
    /**
     * 查询首页小说推荐，并放入缓存中
     */

    @CacheEvict(cacheManager = CacheConsts.CAFFEINE_CACHE_MANAGER,
            value = CacheConsts.HOME_BOOK_CACHE_NAME)
    public void evictCache(){

    }

}
