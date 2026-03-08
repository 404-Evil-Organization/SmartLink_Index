package com.zhilian.zhilianbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhilian.zhilianbackend.mapper")
public class ZhilianBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZhilianBackendApplication.class, args);
	}

}
