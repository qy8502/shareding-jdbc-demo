package com.example.sharedingjdbcdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.sharedingjdbcdemo.dao")
public class SharedingJdbcDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharedingJdbcDemoApplication.class, args);
	}

}
