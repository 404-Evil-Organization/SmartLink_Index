package com.zhilian.zhilianbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: xiaodengyou
 * @Date: 2026/3/14 14:13
 * @Param:
 * @Return:
 * @Description: 资质证书表实体类，对应certification表
 **/
@Getter
@Setter
@Accessors(chain = true)
@TableName("certification")
public class Certification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 证书唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联service_provider.id
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 证书名称（如CNAS、CMA）
     */
    @TableField("cert_name")
    private String certName;

    /**
     * 证书编号
     */
    @TableField("cert_no")
    private String certNo;

    /**
     * 发证机构
     */
    @TableField("issue_authority")
    private String issueAuthority;

    /**
     * 发证日期
     */
    @TableField("issue_date")
    private Date issueDate;

    /**
     * 有效期至
     */
    @TableField("expire_date")
    private Date expireDate;

    /**
     * 证书文件路径
     */
    @TableField("cert_file_url")
    private String certFileUrl;

    /**
     * 状态：0失效 1有效
     */
    @TableField("status")
    private Byte status;

    /**
     * 逻辑删除时间，''1970-01-01 00:00:00'' 表示未删除，其他时间表示已删除
     */
    @TableField("deleted")
    @TableLogic
    private Date deleted;

    /**
     * 上传时间（由自动填充处理）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 最后更新时间（由自动填充处理）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}