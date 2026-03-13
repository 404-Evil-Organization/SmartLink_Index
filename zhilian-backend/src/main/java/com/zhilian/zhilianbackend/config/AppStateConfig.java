package com.zhilian.zhilianbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/13 14:25
 * @Param:
 * @Return:
 * @Description: 应用状态配置，用于控制开发/生产模式
 **/
@Component
@ConfigurationProperties(prefix = "app")
public class AppStateConfig {

    /**
     * 应用运行状态
     * 0: 开发模式 - 所有接口放行
     * 1: 生产模式 - JWT认证
     */
    private String state = "1";

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 14:26
     * @Param:
     * @Return: String 应用状态
     * @Description: 获取应用状态
     **/
    public String getState() {
        return state;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 14:26
     * @Param: state 应用状态
     * @Return:
     * @Description: 设置应用状态（仅允许 0/1，非法值启动失败）
     **/
    public void setState(String state) {
        // 安全校验：只允许 0（开发模式）和 1（生产模式），其他值视为配置错误
        if (!"0".equals(state) && !"1".equals(state)) {
            throw new IllegalArgumentException("app.state 配置非法，只允许 0(开发模式)/1(生产模式)，当前值: " + state);
        }
        this.state = state;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 14:26
     * @Param:
     * @Return: boolean 是否为开发模式
     * @Description: 判断是否为开发模式
     **/
    public boolean isDevMode() {
        return "0".equals(state);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/13 14:26
     * @Param:
     * @Return: boolean 是否为生产模式
     * @Description: 判断是否为生产模式
     **/
    public boolean isProdMode() {
        return "1".equals(state);
    }
}