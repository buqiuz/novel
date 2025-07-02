package io.github.xxyopen.novel.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 全局跨域配置
 *
 * @author xiongxiaoyang
 * @version 1.0
 * @since 2020/5/27
 */
@Configuration
public class NovelCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许 localhost 的任意端口
        config.addAllowedOriginPattern("http://localhost:*");
        // 其他域名
        config.addAllowedOrigin("http://buqiu.icu");
        config.addAllowedOrigin("https://buqiu.icu");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(configurationSource);
    }

}
