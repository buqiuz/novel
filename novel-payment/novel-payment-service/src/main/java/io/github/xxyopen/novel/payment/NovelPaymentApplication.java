package io.github.xxyopen.novel.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication(scanBasePackages = {"io.github.xxyopen.novel"})
@MapperScan("io.github.xxyopen.novel.payment.dao.mapper")
@EnableDiscoveryClient
@EnableCaching
public class NovelPaymentApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(NovelPaymentApplication.class, args);
    }
}
