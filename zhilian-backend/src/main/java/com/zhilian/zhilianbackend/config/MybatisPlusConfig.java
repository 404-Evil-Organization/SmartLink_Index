package com.zhilian.zhilianbackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:52
 * @Param: 
 * @Return: 
 * @Description: MyBatis Plus 配置类，配置分页插件和自动填充功能
**/
@Configuration
public class MybatisPlusConfig {

    /**
     * @Author: 6017
     * @Date: 2026/3/9 20:52
     * @Param: 
     * @Return: MybatisPlusInterceptor MyBatis Plus 拦截器
     * @Description: 配置 MyBatis Plus 拦截器，添加分页插件
    **/
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 显式指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/9 20:53
     * @Param: 
     * @Return: MetaObjectHandler 自动填充处理器
     * @Description: 配置自动填充处理器，自动处理 createTime、updateTime 和 deleted 字段
    **/
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /**
             * @Author: 6017
             * @Date: 2026/3/9 20:53
             * @Param: metaObject 元对象
             * @Return: 
             * @Description: 插入数据时的自动填充处理
            **/
            @Override
            public void insertFill(MetaObject metaObject) {
                // 这些字段名需要和实体类中的属性名一致
                // 使用 strictInsertFill，仅在字段当前为 null 时进行填充，避免覆盖业务侧已显式设置的值
                this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
                this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
                // 逻辑删除标记不在这里默认填充，保持由数据库默认值或逻辑删除注解/业务逻辑控制
            }

            /**
             * @Author: 6017
             * @Date: 2026/3/9 20:56
             * @Param:  metaObject 元对象
             * @Return: 
             * @Description: 更新数据时的自动填充处理
            **/
            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时统一刷新更新时间字段，仅在当前值为 null 时填充，避免覆盖业务侧特殊处理
                this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
            }
        };
    }
}