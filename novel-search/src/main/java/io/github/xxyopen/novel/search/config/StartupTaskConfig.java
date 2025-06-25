package io.github.xxyopen.novel.search.config;

import io.github.xxyopen.novel.search.task.BookToEsTask;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * 类描述
 * </p>
 *
 * @author: 不秋
 * @since: 2025-06-24 17:14:04
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class StartupTaskConfig {

    private final BookToEsTask bookToEsTask;

    @Bean
    public ApplicationRunner runSaveToEsOnStartup() {
        return args -> {
            log.info(">>> 应用启动完成，执行小说数据同步到 Elasticsearch...");
            bookToEsTask.saveToEs();
        };
    }
}
