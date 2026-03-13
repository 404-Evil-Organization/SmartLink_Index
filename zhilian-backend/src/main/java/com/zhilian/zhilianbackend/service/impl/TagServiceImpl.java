package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.TagResponse;
import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.mapper.TagMapper;
import com.zhilian.zhilianbackend.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.zhilian.zhilianbackend.exception.BusinessException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:49
 * @Param:
 * @Return: 
 * @Description: 标签字典表业务逻辑实现类，实现标签相关的业务方法
**/
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:45
     * @Param: queryRequest 分页查询参数（包含页码、每页条数、标签名称模糊查询、类别筛选）
     * @Return: IPage<TagResponse> 分页后的标签列表
     * @Description: 分页查询标签，支持按名称模糊查询和按类别精确筛选
    **/
    @Override
    public IPage<TagResponse> pageQuery(TagQueryRequest queryRequest) {
        log.info("分页查询标签，参数：page={}, size={}, name={}, category={}",
                queryRequest.getPage(), queryRequest.getSize(),
                queryRequest.getName(), queryRequest.getCategory());

        // 1. 构建分页对象
        Page<Tag> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryRequest.getName()), Tag::getName, queryRequest.getName())
                .eq(StringUtils.hasText(queryRequest.getCategory()), Tag::getCategory, queryRequest.getCategory())
                .orderByDesc(Tag::getCreateTime);

        // 3. 执行查询
        Page<Tag> tagPage = this.page(page, wrapper);

        // 4. 转换为Response
        IPage<TagResponse> resultPage = tagPage.convert(tag -> {
            TagResponse response = new TagResponse();
            BeanUtils.copyProperties(tag, response);
            return response;
        });

        log.info("查询到标签数据 {} 条，总记录数 {}", resultPage.getRecords().size(), resultPage.getTotal());
        return resultPage;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:46
     * @Param: request 新增标签请求参数（包含标签名称、类别、说明）
     * @Return: Long 新增标签的ID
     * @Description: 新增标签，会校验标签名称是否已存在
    **/
    @Override
    public Long addTag(TagRequest request) {
        log.info("新增标签，参数：name={}, category={}, description={}",
                request.getName(), request.getCategory(), request.getDescription());

        // 1. 参数校验
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("标签名称不能为空");
        }

        // 2. 基于 (name, category) 维度做本地互斥，避免“先查后插”并发竞态
        String categoryKey = StringUtils.hasText(request.getCategory()) ? request.getCategory() : "";
        String lockKey = (request.getName() + "::" + categoryKey).intern();
        synchronized (lockKey) {
            // 2.1 再次检查标签名在当前分类下是否已存在（在锁内保证串行）
            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getName, request.getName())
                    .eq(StringUtils.hasText(request.getCategory()), Tag::getCategory, request.getCategory());

            long count = this.count(wrapper);
            if (count > 0) {
                throw new RuntimeException("标签名称已存在"); // 可以换成 BusinessException
            }

            // 3. 转换为实体并保存
            Tag tag = new Tag();
            BeanUtils.copyProperties(request, tag);
            this.save(tag);

            log.info("标签新增成功，ID：{}", tag.getId());
            return tag.getId();
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:46
     * @Param: id 要修改的标签ID;request 修改标签请求参数（只传需要修改的字段）
     * @Return: 
     * @Description: 修改标签信息，如果修改名称会检查新名称是否与其他标签冲突
    **/
    @Override
    public void updateTag(Long id, TagRequest request) {
        log.info("修改标签，ID：{}，参数：name={}, category={}, description={}",
                id, request.getName(), request.getCategory(), request.getDescription());

        // 1. 检查标签是否存在
        Tag existingTag = this.getById(id);
        if (existingTag == null) {
            // 标签不存在属于业务异常，返回 404 状态码，便于前端区分资源不存在场景
            throw new BusinessException(404, "标签不存在");
        }

        // 2. 如果修改了名称，检查新名称是否与其他标签冲突
        if (StringUtils.hasText(request.getName()) && !request.getName().equals(existingTag.getName())) {
            // 生效的分类：请求中有传则用请求值，否则沿用原标签的分类，避免只按 name 全局查重
            String targetCategory = StringUtils.hasText(request.getCategory())
                    ? request.getCategory()
                    : existingTag.getCategory();

            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getName, request.getName())
                    // 按 (name, category) 维度查重；如果分类为空，则仅按名称查重
                    .eq(StringUtils.hasText(targetCategory), Tag::getCategory, targetCategory)
                    .ne(Tag::getId, id); // 排除自身

            long count = this.count(wrapper);
            if (count > 0) {
                // 标签名称已存在属于冲突场景，返回 409 状态码，与 Result 约定保持一致
                throw new BusinessException(409, "标签名称已存在");
            }
        }

        // 3. 更新字段（只更新有值的字段）
        Tag tag = new Tag();
        tag.setId(id);

        if (StringUtils.hasText(request.getName())) {
            tag.setName(request.getName());
        }
        if (StringUtils.hasText(request.getCategory())) {
            tag.setCategory(request.getCategory());
        }
        if (StringUtils.hasText(request.getDescription())) {
            tag.setDescription(request.getDescription());
        }

        this.updateById(tag);
        log.info("标签修改成功，ID：{}", id);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:47
     * @Param: id 要删除的标签ID
     * @Return: 
     * @Description: 逻辑删除标签（@TableLogic 注解自动处理）
    **/
    @Override
    public void deleteTag(Long id) {
        log.info("删除标签，ID：{}", id);

        // 1. 检查标签是否存在
        Tag existingTag = this.getById(id);
        if (existingTag == null) {
            throw new RuntimeException("标签不存在，ID：" + id);
        }

        // 2. 逻辑删除（@TableLogic 注解会自动处理）
        boolean removed = this.removeById(id);

        if (removed) {
            log.info("标签删除成功，ID：{}", id);
        } else {
            log.warn("标签删除失败，ID：{}", id);
            throw new RuntimeException("标签删除失败");
        }
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:47
     * @Param: id 要查询的标签ID
     * @Return: TagResponse 标签详情
     * @Description: 根据ID获取标签详细信息
    **/
    @Override
    public TagResponse getTagDetail(Long id) {
        log.info("查询标签详情，ID：{}", id);

        // 1. 查询标签
        Tag tag = this.getById(id);
        if (tag == null) {
            throw new RuntimeException("标签不存在，ID：" + id);
        }

        // 2. 转换为Response
        TagResponse response = new TagResponse();
        BeanUtils.copyProperties(tag, response);

        log.info("标签详情查询成功，ID：{}，名称：{}", id, tag.getName());
        return response;
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/12 23:48
     * @Param: 
     * @Return: List<TagResponse> 服务标签列表
     * @Description: 获取所有类别为'service'的标签，用于服务商的服务类型多选
    **/
    public List<TagResponse> getServiceTags() {
        log.info("查询服务标签列表");

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getCategory, "service")  // 只查询服务类型的标签
                .orderByAsc(Tag::getName);

        List<Tag> tags = this.list(wrapper);

        List<TagResponse> responses = tags.stream()
                .map(tag -> {
                    TagResponse response = new TagResponse();
                    BeanUtils.copyProperties(tag, response);
                    return response;
                })
                .collect(Collectors.toList());

        log.info("查询到服务标签 {} 条", responses.size());
        return responses;
    }
}
