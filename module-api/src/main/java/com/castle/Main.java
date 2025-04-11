package com.castle;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        initEnv();
        SpringApplication.run(Main.class, args);
    }

    /**
     * 스프링 부트 실행 전 시스템 property를 설정한다.
     */
    static void initEnv() {
        Dotenv.configure()
                .directory("./")
                .filename(".env")
                .load()
                .entries()
                .forEach(e -> {
                    System.setProperty(e.getKey(), e.getValue());
                });
    }
}