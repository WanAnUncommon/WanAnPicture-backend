package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 主类（项目启动入口）
 *
 * @author WanAn
 */
@SpringBootApplication
@MapperScan("com.example.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableAsync
public class WanAnPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanAnPictureBackendApplication.class, args);
    }

}
