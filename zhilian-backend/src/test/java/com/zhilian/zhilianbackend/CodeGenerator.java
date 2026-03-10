package com.zhilian.zhilianbackend;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/smartlink_index?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
        String username = "smartlink_index";
        String password = "smartlink_index_2026";

        // 项目路径
        String projectPath = System.getProperty("user.dir");

        FastAutoGenerator.create(url, username, password)
                // 全局配置
                .globalConfig(builder -> {
                    builder.author("智链团队") // 设置作者
                            .outputDir(projectPath + "/src/main/java") // 输出目录
                            .dateType(DateType.ONLY_DATE) // 使用java.util.Date匹配数据库DATETIME
                            .commentDate("yyyy-MM-dd")
                            .disableOpenDir(); // 生成后不打开目录
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent("com.zhilian") // 父包名
                            .moduleName("zhilianbackend") // 模块名
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .xml("mapper.xml") // mapper.xml文件
                            .pathInfo(Collections.singletonMap(OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper")); // mapper.xml路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    builder.addInclude( // 添加需要生成的表
                                    "user", "manufacture", "service_provider", "demand", "tag",
                                    "demand_tag", "service_tag", "cooperation", "evaluation",
                                    "diagnosis", "region_index", "certification", "credit_score", "abroad_case"
                            )
                            .addTablePrefix() // 表前缀过滤

                            // Entity策略
                            .entityBuilder()
                            .enableLombok() // 启用Lombok
                            .enableChainModel() // 链式模型
                            .enableTableFieldAnnotation() // 启用字段注解
                            .logicDeleteColumnName("deleted") // 逻辑删除字段
                            .formatFileName("%s")

                            // Mapper策略
                            .mapperBuilder()
                            .enableBaseResultMap() // 生成通用的resultMap
                            .enableBaseColumnList() // 生成通用的columnList
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper")

                            // Service策略
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")

                            // Controller策略
                            .controllerBuilder()
                            .enableRestStyle() // 启用REST风格
                            .enableHyphenStyle() // 启用连字符
                            .formatFileName("%sController");
                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Velocity模板引擎
                .execute();
    }
}