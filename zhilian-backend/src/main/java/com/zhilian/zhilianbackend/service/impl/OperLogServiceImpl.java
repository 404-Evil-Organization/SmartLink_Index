package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhilian.zhilianbackend.dto.response.OperLogVO;
import com.zhilian.zhilianbackend.entity.OperLog;
import com.zhilian.zhilianbackend.entity.User;
import com.zhilian.zhilianbackend.exception.BusinessException;
import com.zhilian.zhilianbackend.mapper.OperLogMapper;
import com.zhilian.zhilianbackend.mapper.UserMapper;
import com.zhilian.zhilianbackend.service.OperLogService;
import com.zhilian.zhilianbackend.utils.SecurityUtils;
import com.zhilian.zhilianbackend.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Objects;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25 22:45
 * @Description: 操作日志表业务逻辑实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25 22:45
     * @Param: page 当前页码
     * @Param: size 每页条数
     * @Param: username 操作人用户名（模糊匹配）
     * @Param: operation 操作类型（精确匹配）
     * @Param: startTime 开始时间
     * @Param: endTime 结束时间
     * @Return: IPage<OperLogVO> 分页结果（VO 类型）
     * @Description: 分页查询操作日志，支持多条件筛选，并将实体转换为 VO
     */
    @Override
    public IPage<OperLogVO> listOperLogs(Integer page, Integer size, String username, String operation,
                                         Date startTime, Date endTime) {
        // 分页参数兜底与范围校验，防止空指针与异常分页
        long safePage = (page == null || page < 1) ? 1L : page.longValue();
        long safeSize;
        if (size == null || size < 1) {
            // 默认每页 10 条
            safeSize = 10L;
        } else if (size > 100) {
            // 限制单页最大 100 条，避免一次性查询过多数据
            safeSize = 100L;
        } else {
            safeSize = size.longValue();
        }
        // 构建分页对象（使用安全的分页参数）
        Page<OperLog> pageParam = new Page<>(safePage, safeSize);

        // 构建查询条件
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名模糊匹配（转义通配符，防止 SQL 注入/通配符放大）
        if (StringUtils.isNotBlank(username)) {
            String escaped = SqlUtils.escapeSqlLike(username);
            wrapper.apply("username LIKE CONCAT('%', {0}, '%') ESCAPE '\\\\'", escaped);
        }

        // 操作类型精确匹配
        if (StringUtils.isNotBlank(operation)) {
            wrapper.eq(OperLog::getOperation, operation);
        }

        // 时间范围查询
        if (Objects.nonNull(startTime)) {
            wrapper.ge(OperLog::getCreateTime, startTime);
        }
        if (Objects.nonNull(endTime)) {
            wrapper.le(OperLog::getCreateTime, endTime);
        }

        // 按创建时间倒序排序（最新的在前）
        wrapper.orderByDesc(OperLog::getCreateTime);

        // 执行分页查询
        IPage<OperLog> entityPage = this.baseMapper.selectPage(pageParam, wrapper);

        // 使用 convert 方法将实体转换为 VO，保持分页元信息不变
        return entityPage.convert(this::convertToVO);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/25 22:45
     * @Param: entity 操作日志实体
     * @Return: OperLogVO 操作日志 VO
     * @Description: 将实体转换为 VO，隐藏内部字段
     */
    private OperLogVO convertToVO(OperLog entity) {
        if (entity == null) {
            return null;
        }
        OperLogVO vo = new OperLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:16
     * @Param: operation 操作描述（如：用户登录、新增需求等）
     * @Param: params 请求参数的 JSON 字符串（可选）
     * @Param: result 操作结果（成功/失败）
     * @Param: errorMsg 失败时的详细原因或异常信息（可选）
     * @Description: 记录操作日志，该方法会自动从安全上下文中获取当前登录用户的信息，
     *               并从 HTTP 请求上下文中获取真实的客户端 IP 地址。如果处于未登录状态（如登录接口），
     *               则尝试从传入的 JSON 参数中解析出用户名。
     */
    @Override
    public void recordLog(String operation, String params, String result, String errorMsg) {
        try {
            OperLog operLog = new OperLog();
            operLog.setOperation(operation);
            operLog.setParams(params);
            operLog.setResult(result);
            operLog.setErrorMsg(errorMsg);
            operLog.setCreateTime(new Date());

            // 获取当前登录用户
            Long userId = securityUtils.getCurrentUserIdOrNull();
            if (userId != null) {
                operLog.setUserId(userId);
                
                // 优先从 SecurityContext 的 details 中获取用户名（避免查库）
                String username = securityUtils.getCurrentUsernameOrNull();
                if (StringUtils.isNotBlank(username)) {
                    operLog.setUsername(username);
                } else {
                    // 如果上下文没有用户名，作为后备方案再去查库
                    User user = userMapper.selectById(userId);
                    if (user != null) {
                        operLog.setUsername(user.getUsername());
                    }
                }
            } else {
                // 如果未登录（如登录接口），尝试从 params 中解析用户名
                if (StringUtils.isNotBlank(params)) {
                    try {
                        JsonNode jsonNode = objectMapper.readTree(params);
                        // 寻找 request 对象中的 username 字段
                        JsonNode requestNode = jsonNode.get("request");
                        if (requestNode != null && requestNode.has("username")) {
                            operLog.setUsername(requestNode.get("username").asText());
                        }
                    } catch (Exception e) {
                        log.debug("尝试从 params 解析用户名失败", e);
                    }
                }
            }

            // 获取客户端IP
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = getClientIp(request);
                operLog.setIp(ip);
                
                // 如果 userId 为空（比如登录时 SecurityContext 还没有设值），尝试从请求中获取用户名（如果是登录请求）
                if (userId == null && StringUtils.isBlank(operLog.getUsername())) {
                    try {
                        String reqUsername = request.getParameter("username");
                        if (StringUtils.isNotBlank(reqUsername)) {
                            operLog.setUsername(reqUsername);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }

            this.save(operLog);
        } catch (Exception e) {
            // 日志记录失败不影响主业务，仅打印错误日志
            log.error("记录操作日志失败: operation={}, error={}", operation, e.getMessage(), e);
        }
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:16
     * @Param: request 当前 HTTP 请求对象
     * @Return: String 真实的客户端 IP 地址
     * @Description: 获取客户端真实 IP 地址，通过解析多个常见的反向代理头信息（如 X-Forwarded-For），
     *               并处理多级代理以及 IPv6 本地回环地址的情况，返回最原始的客户端 IP。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多次反向代理后会有多个IP值，第一个为真实IP
        if (StringUtils.isNotEmpty(ip) && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        
        // 处理 IPv6 的本地回环地址
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        
        return ip;
    }

    /**
     * @Author: taciturn-hg
     * @Date: 2026/03/27 9:17
     * @Param: id 操作日志在数据库中的主键 ID
     * @Return: OperLogVO 转换后的操作日志详情视图对象
     * @Description: 根据 ID 查询并获取操作日志的详细信息，若记录不存在则抛出 404 业务异常。
     */
    @Override
    public OperLogVO getOperLogDetail(Long id) {
        OperLog operLog = this.getById(id);
        if (operLog == null) {
            throw new BusinessException(404, "操作日志不存在");
        }
        return convertToVO(operLog);
    }
}