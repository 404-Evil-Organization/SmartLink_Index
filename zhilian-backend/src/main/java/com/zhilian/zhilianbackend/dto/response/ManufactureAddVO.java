package com.zhilian.zhilianbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: xiaodengyou
 * @Date: 2026-03-12 22:31
 * @Param:
 * @Return:
 * @Description: 新增制造企业返回对象
 **/
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新增制造企业返回对象")
public class ManufactureAddVO {

    @Schema(description = "新创建的企业ID", example = "1010")
    private Long id;
}