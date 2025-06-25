package io.github.xxyopen.novel.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import io.github.xxyopen.novel.book.dto.resp.BookEsRespDto;
import io.github.xxyopen.novel.search.constant.EsConsts;
import io.github.xxyopen.novel.search.service.BookEsSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-24 17:39:34
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookEsSyncServiceImpl implements BookEsSyncService {
    /**
     * 同步单本小说到 Elasticsearch
     *
     */
    private final ElasticsearchClient elasticsearchClient;

    public void syncBook(BookEsRespDto book) {
        try {
            IndexRequest<BookEsRespDto> request = IndexRequest.of(b -> b
                    .index(EsConsts.BookIndex.INDEX_NAME)
                    .id(book.getId().toString())
                    .document(book)
            );
            elasticsearchClient.index(request);
        } catch (Exception e) {
            log.error("同步单本小说到 ES 失败，bookId={}", book.getId(), e);
            throw new RuntimeException("同步 Elasticsearch 失败", e);
        }
    }
}
