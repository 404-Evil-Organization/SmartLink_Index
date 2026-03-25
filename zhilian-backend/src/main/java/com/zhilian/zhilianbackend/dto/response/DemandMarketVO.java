package com.zhilian.zhilianbackend.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/25
 * @Description: 市场需求列表项视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandMarketVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal expectedBudget;
    private Date deadline;          // 原 LocalDate -> Date
    private Date createTime;        // 原 LocalDateTime -> Date
    private ManufactureListVO manufacture;
    private List<TagResponse> tags;
}