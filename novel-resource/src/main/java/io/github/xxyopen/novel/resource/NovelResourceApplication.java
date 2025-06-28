package io.github.xxyopen.novel.resource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"io.github.xxyopen.novel"})
@EnableDiscoveryClient
@MapperScan("io.github.xxyopen.novel.resource.dao.mapper")
public class NovelResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelResourceApplication.class, args);
    }

}
