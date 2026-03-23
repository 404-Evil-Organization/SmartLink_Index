package com.zhilian.zhilianbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.result.PageResult;
import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.dto.request.UserListRequest;
import com.zhilian.zhilianbackend.dto.request.UserStatusUpdateRequest;
import com.zhilian.zhilianbackend.dto.response.ResetPasswordVO;
import com.zhilian.zhilianbackend.dto.response.UserDetailVO;
import com.zhilian.zhilianbackend.dto.response.UserListVO;
import com.zhilian.zhilianbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "管理员用户管理")
public class AdminUserController {

    private final UserService userService;

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: request 用户列表查询参数（分页、筛选）
     * @Return: Result<PageResult<UserListVO>> 分页用户列表
     * @Description: 获取用户列表（分页），仅管理员可访问
     */
    @Operation(summary = "获取用户列表（分页）")
    @GetMapping("/list")
    public Result<PageResult<UserListVO>> listUsers(@Valid UserListRequest request) {
        Page<UserListVO> page = userService.pageUsers(request);
        PageResult<UserListVO> pageResult = PageResult.from(page);
        return Result.success(pageResult);
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: id 用户ID
     * @Return: Result<UserDetailVO> 用户详情（含企业信息）
     * @Description: 获取指定用户的详细信息，仅管理员可访问
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserDetail(id));
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: id 用户ID
     * @Param: request 修改状态请求体（包含状态值）
     * @Return: Result<Void>
     * @Description: 修改用户状态（启用/禁用），仅管理员可操作
     */
    @Operation(summary = "修改用户状态（启用/禁用）")
    @PutMapping("/status/{id}")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                         @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return Result.success();
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026/3/21 15:04
     * @Param: id 用户ID
     * @Return: Result<ResetPasswordVO> 包含新生成的临时密码
     * @Description: 重置用户密码（生成随机密码），仅管理员可操作
     */
    @Operation(summary = "重置用户密码")
    @PostMapping("/reset-password/{id}")
    public Result<ResetPasswordVO> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetUserPassword(id);
        ResetPasswordVO vo = new ResetPasswordVO();
        vo.setNewPassword(newPassword);
        return Result.success(vo);
    }
}