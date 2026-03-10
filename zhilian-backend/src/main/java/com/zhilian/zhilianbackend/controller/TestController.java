package com.zhilian.zhilianbackend.controller;

import com.zhilian.zhilianbackend.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 6017
 * @Date: 2026/3/9 23:12
 * @Param:
 * @Return:
 * @Description: 测试控制器，用于前端联调验证
**/
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试接口 - 返回Hello消息
     */
    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Hello from backend!");
        data.put("timestamp", System.currentTimeMillis());

        // 使用简洁的成功响应
        return Result.success(data);
    }

    /**
     * 测试接口 - 返回服务器时间
     */
    @GetMapping("/time")
    public Result<Map<String, String>> time() {
        Map<String, String> data = new HashMap<>();
        data.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        // 使用自定义消息的成功响应
        return Result.success("获取服务器时间成功", data);
    }

    /**
     * 测试接口 - 回显请求参数
     */
    @GetMapping("/echo")
    public Result<Map<String, String>> echo(@RequestParam(required = false) String message) {
        Map<String, String> data = new HashMap<>();
        data.put("echo", message != null ? message : "没有收到消息");
        data.put("received", message != null ? "true" : "false");

        return Result.success(data);
    }

    /**
     * 测试接口 - 模拟各种错误响应
     */
    @GetMapping("/error-test")
    public Result<String> errorTest(@RequestParam(defaultValue = "400") String type) {
        switch (type) {
            case "400":
                return Result.badRequest("请求参数错误");
            case "401":
                return Result.unauthorized("未授权，请先登录");
            case "403":
                return Result.forbidden("禁止访问");
            case "404":
                return Result.notFound("资源不存在");
            case "409":
                return Result.conflict("数据冲突，请重试");
            case "429":
                return Result.tooManyRequests("请求过于频繁，请稍后再试");
            case "500":
                return Result.error("服务器内部错误");
            default:
                return Result.success("一切正常");
        }
    }

    /**
     * 测试接口 - 返回复杂数据结构
     */
    @GetMapping("/user-info")
    public Result<Map<String, Object>> userInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1001);
        data.put("username", "test_user");
        data.put("nickname", "测试用户");
        data.put("roles", new String[]{"admin", "user"});

        // 添加一些嵌套数据
        Map<String, Object> profile = new HashMap<>();
        profile.put("avatar", "https://example.com/avatar.jpg");
        profile.put("email", "test@example.com");
        profile.put("phone", "13800138000");

        data.put("profile", profile);

        return Result.success("获取用户信息成功", data);
    }

    /**
     * 测试接口 - 成功但无数据返回
     */
    @GetMapping("/success-no-data")
    public Result<Void> successNoData() {
        // 返回成功，无数据
        return Result.success();
    }

    /**
     * 测试接口 - 成功且只有消息
     */
    @GetMapping("/success-with-message")
    public Result<Void> successWithMessage() {
        // 返回成功，自定义消息，无数据
        return Result.success("操作成功完成");
    }

    /**
     * 测试接口 - 自定义状态码
     */
    @GetMapping("/custom-status")
    public Result<String> customStatus(@RequestParam(defaultValue = "200") Integer code) {
        if (code == 200) {
            return Result.success("自定义成功");
        } else {
            // 使用 of 方法自定义状态码和消息
            return Result.of(code, "自定义状态码：" + code, null);
        }
    }

    /**
     * 测试接口 - 分页数据模拟
     */
    @GetMapping("/page-data")
    public Result<Map<String, Object>> pageData(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> data = new HashMap<>();
        data.put("current", page);
        data.put("size", size);
        data.put("total", 100);

        // 模拟数据列表
        Object[] records = new Object[size];
        for (int i = 0; i < size; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", (page - 1) * size + i + 1);
            record.put("name", "测试数据" + ((page - 1) * size + i + 1));
            records[i] = record;
        }
        data.put("records", records);

        return Result.success(data);
    }

    /**
     * 测试接口 - 验证 isSuccess() 方法
     * 这个接口返回的数据中会包含一个 success 字段，前端可以根据这个判断
     */
    @GetMapping("/check-success")
    public Result<Map<String, Object>> checkSuccess() {
        Map<String, Object> data = new HashMap<>();

        // 创建一个成功的结果
        Result<String> successResult = Result.success("测试数据");
        data.put("isSuccess", successResult.isSuccess());
        data.put("isError", successResult.isError());

        // 创建一个错误的结果
        Result<String> errorResult = Result.error("测试错误");
        data.put("errorResult", Map.of(
                "isSuccess", errorResult.isSuccess(),
                "isError", errorResult.isError(),
                "code", errorResult.getCode(),
                "message", errorResult.getMessage()
        ));

        return Result.success(data);
    }
}