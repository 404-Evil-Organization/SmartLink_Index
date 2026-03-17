package com.zhilian.zhilianbackend.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/12 20:25
 * @Param:
 * @Return:
 * @Description: 分页结果封装类
 **/
@Getter
@Setter
@ToString
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "当前页码，从 1 开始")
    private Long page;

    @Schema(description = "每页条数")
    private Long size;


    public PageResult() {
    }

    public PageResult(Long total, List<T> records, Long page, Long size) {
        this.total = total;
        this.records = records;
        this.page = page;
        this.size = size;
    }

    /**
     * @Author: xiaodengyou
     * @Date: 2026-03-12 14:32
     * @Param: page MyBatis Plus 分页对象
     * @Return: PageResult<T> 分页结果
     * @Description: 从 MyBatis Plus 的 IPage 对象构建分页结果
     **/
    public static <T> PageResult<T> from(IPage<T> page) {

        return new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
    }
}