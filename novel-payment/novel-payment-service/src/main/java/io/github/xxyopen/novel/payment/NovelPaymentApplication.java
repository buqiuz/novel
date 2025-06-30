package io.github.xxyopen.novel.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(scanBasePackages = {"io.github.xxyopen.novel"})
@MapperScan("io.github.xxyopen.novel.payment.dao.mapper")
@EnableFeignClients(basePackages = "io.github.xxyopen.novel.book.feign")
@EnableDiscoveryClient
@EnableCaching
public class NovelPaymentApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(NovelPaymentApplication.class, args);
    }
}
