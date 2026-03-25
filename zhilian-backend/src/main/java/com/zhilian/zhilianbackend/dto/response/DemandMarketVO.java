package com.zhilian.zhilianbackend.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandMarketVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal expectedBudget;
    private LocalDate deadline;
    private LocalDateTime createTime;
    private ManufactureListVO manufacture;
    private List<TagResponse> tags;
}