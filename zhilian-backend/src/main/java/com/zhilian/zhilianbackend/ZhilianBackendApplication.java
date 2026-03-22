package com.zhilian.zhilianbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@MapperScan("com.zhilian.zhilianbackend.mapper")
public class ZhilianBackendApplication {
	/**
	 * 应用启动入口
	 */
	public static void main(String[] args) {
		SpringApplication.run(ZhilianBackendApplication.class, args);
	}
	/**
	 * 定时任务调度配置：
	 * - 通过 @EnableScheduling 开启 Spring Scheduling 功能
	 * - 通过 @ConditionalOnProperty 增加统一开关，避免在测试/本地环境无条件执行定时任务
	 *
	 * 配置说明：
	 * - 属性名：app.scheduling.enabled
	 * - 当为 true 或未配置时（matchIfMissing = true），启用定时任务（保持当前默认行为）
	 * - 当为 false 时，禁用所有基于 @Scheduled 的定时任务
	 */
	@Configuration
	@EnableScheduling
	@ConditionalOnProperty(
			prefix = "app.scheduling",
			name = "enabled",
			havingValue = "true",
			matchIfMissing = true
	)
	static class SchedulingConfiguration {
		// 该配置类仅用于受控开启定时任务，无需额外 Bean 定义
	}


}
