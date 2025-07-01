package io.github.xxyopen.novel.search.service;

import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;

/**
 * <p>
 * 接口描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-24 17:40:00
 */
public interface BookEsSyncService {
    /**
     * 同步单本小说到 Elasticsearch
     *
     * @param book 小说ID
     */
    void syncBook(BookEsRespDto book);

    void removeBook(Long bookId);
}
