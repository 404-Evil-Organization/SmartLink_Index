package com.zhilian.zhilianbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 开启定时任务
@MapperScan("com.zhilian.zhilianbackend.mapper")
public class ZhilianBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(ZhilianBackendApplication.class, args);
	}


}
