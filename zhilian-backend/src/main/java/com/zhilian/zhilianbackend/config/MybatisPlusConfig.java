package com.zhilian.zhilianbackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 显式指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 这些字段名需要和实体类中的属性名一致
                // 这里使用 setFieldValByName，避免依赖实体类上的 @TableField(fill = ...) 注解
                this.setFieldValByName("createTime", new Date(), metaObject);
                this.setFieldValByName("updateTime", new Date(), metaObject);
                // 逻辑删除标记保持为 null，通常由数据库默认值或业务逻辑进行设置
                this.setFieldValByName("deleted", null, metaObject);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时统一刷新更新时间字段
                this.setFieldValByName("updateTime", new Date(), metaObject);
            }
        };
    }
}