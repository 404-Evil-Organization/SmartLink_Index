package com.zhilian.zhilianbackend.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zhilianbackend.annotation.LogOperation;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.service.OperLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: taciturn-hg
 * @Date: 2026/03/27 9:05
 * @Description: 操作日志切面，拦截 @LogOperation 注解的方法，自动记录请求参数、执行结果及异常信息
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogService operLogService;
    private final ObjectMapper objectMapper;

    /**
     * @Author:taciturn-hg
     * @Date: 2026/03/27 9:16
     * @Param: joinPoint 切点，包含了目标方法的信息和参数
     * @Param: logOperation 方法上的日志注解实例，包含操作描述
     * @Return: Object 目标方法的执行结果
     * @Description: 环绕通知，在目标方法执行前后进行拦截，获取参数、执行方法，并根据是否抛出异常来记录成功或失败的操作日志。
     */
    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        String operation = logOperation.value();
        String paramsStr = getParamsStr(joinPoint);
        Object resultObj = null;
        String errorMsg = null;
        String resultStatus = "成功";

        try {
            // 执行目标方法
            resultObj = joinPoint.proceed();
            return resultObj;
        } catch (BusinessException be) {
            resultStatus = "失败";
            errorMsg = be.getMessage();
            throw be;
        } catch (Throwable e) {
            resultStatus = "失败";
            errorMsg = "系统异常: " + e.getMessage();
            throw e;
        } finally {
            // 无论成功还是失败，都在 finally 中记录日志
            try {
                operLogService.recordLog(operation, paramsStr, resultStatus, errorMsg);
            } catch (Throwable e) {
                log.error("AOP记录操作日志失败", e);
            }
        }
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:16
     * @Param: joinPoint 切点对象
     * @Return: String 序列化后的 JSON 字符串，如果参数为空则返回 "{}"
     * @Description: 解析方法参数，将其转为 JSON 字符串。过滤掉不可序列化的 ServletRequest/Response 和 MultipartFile 等类型，防止序列化异常。
     */
    private String getParamsStr(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (args == null || args.length == 0) {
                return "{}";
            }

            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                // 过滤掉不能被序列化的参数类型
                if (arg instanceof HttpServletRequest || 
                    arg instanceof HttpServletResponse || 
                    arg instanceof MultipartFile || 
                    arg instanceof MultipartFile[]) {
                    paramMap.put(paramNames[i], "[Ignored " + arg.getClass().getSimpleName() + "]");
                    continue;
                }
                paramMap.put(paramNames[i], arg);
            }

            return objectMapper.writeValueAsString(paramMap);
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
            return "{\"error\":\"参数序列化失败\"}";
        }
    }
}