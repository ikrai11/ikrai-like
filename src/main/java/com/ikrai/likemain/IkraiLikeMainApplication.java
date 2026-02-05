package com.ikrai.likemain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ikrai.likemain.mapper")
public class IkraiLikeMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(IkraiLikeMainApplication.class, args);
    }

}
