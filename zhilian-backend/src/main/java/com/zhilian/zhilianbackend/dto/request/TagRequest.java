package com.zhilian.zhilianbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/12 22:51
 * @Param:
 * @Return:
 * @Description: 标签新增/修改请求参数
 **/
@Data
@Schema(description = "标签新增/修改请求参数")
public class TagRequest {

    /**
     * 标签名称
     * 新增场景：必填且不能为空串；
     * 修改场景：若传入则必须满足非空和长度约束。
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    @Schema(description = "标签名称", required = false, example = "CNAS认证")
    private String name;

    /**
     * 标签类别（可选），限制最大长度防止异常输入。
     */
    @Size(max = 30, message = "标签类别长度不能超过30个字符")
    @Schema(description = "标签类别", example = "certification")
    private String category;

    /**
     * 标签说明（可选），限制最大长度与数据库字段保持一致。
     */
    @Size(max = 255, message = "标签说明长度不能超过255个字符")
    @Schema(description = "标签说明", example = "中国合格评定国家认可委员会认证")
    private String description;
}
