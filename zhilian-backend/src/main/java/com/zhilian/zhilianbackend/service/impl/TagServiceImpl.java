package com.zhilian.zhilianbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhilian.zhilianbackend.common.enums.TagCategory;
import com.zhilian.zhilianbackend.dto.request.TagQueryRequest;
import com.zhilian.zhilianbackend.dto.request.TagRequest;
import com.zhilian.zhilianbackend.dto.response.ServiceTagResponse;
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

        Page<Tag> page = new Page<>(queryRequest.getPage(), queryRequest.getSize());

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryRequest.getName()), Tag::getName, queryRequest.getName())
                .eq(StringUtils.hasText(queryRequest.getCategory()), Tag::getCategory, queryRequest.getCategory())
                .orderByDesc(Tag::getCreateTime);

        Page<Tag> tagPage = this.page(page, wrapper);

        IPage<TagResponse> resultPage = tagPage.convert(tag -> {
            TagResponse response = new TagResponse();
            BeanUtils.copyProperties(tag, response);
            // 可以在这里添加中文描述的转换，但建议留给前端或单独接口
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
            throw new BusinessException(400, "标签名称不能为空");
        }

        // 2. 校验category是否有效
        String category = request.getCategory();
        if (!StringUtils.hasText(category)) {
            // 未传入类别时默认使用 GENERAL（"general"），与数据库 schema.sql 中 tag.category 默认值保持一致
            category = TagCategory.GENERAL.getValue();
        } else if (!TagCategory.isValid(category)) {
            throw new BusinessException(400, "无效的标签类别，可选值：" +
                    String.join(", ", TagCategory.getAllValues()));
        } else {
            // 对传入的合法类别做主值归一化，例如将 rests/general 等别名统一映射为 GENERAL
            category = TagCategory.fromValue(category).getValue();
        }

        // 3. 检查标签名是否已存在（必须同时检查 name 和 category，且 category 使用归一化后的主值）
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, request.getName())
                .eq(Tag::getCategory, category);

        long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException(400, "标签名称已存在");
        }

        // 4. 转换为实体并保存（持久化的 category 也使用归一化后的主值）
        Tag tag = new Tag();
        BeanUtils.copyProperties(request, tag);
        tag.setCategory(category);

        // 注意：必须检查 save 返回值，防止插入失败却继续返回 null ID 导致上层误判
        boolean saved = this.save(tag);
        if (!saved) {
            log.error("新增标签持久化失败，name={}，category={}", request.getName(), category);
            throw new BusinessException(500, "新增标签失败，请稍后重试");
        }

        // 再次校验 ID 是否成功回填，避免因主键未生成导致业务误判
        if (tag.getId() == null) {
            log.error("新增标签后主键ID未回填，name={}，category={}", request.getName(), category);
            throw new BusinessException(500, "新增标签失败（ID 未生成），请联系管理员");
        }

        log.info("标签新增成功，ID：{}，category：{}", tag.getId(), category);
        return tag.getId();
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
            // 使用业务异常返回 404，表示标签资源不存在
            throw new BusinessException(404, "标签不存在，ID：" + id);
        }

        // 2. 校验category是否有效（如果传了的话）
        if (StringUtils.hasText(request.getCategory()) && !TagCategory.isValid(request.getCategory())) {
            // 使用业务异常返回 400，表示请求参数（标签类别）不合法
            throw new BusinessException(400, "无效的标签类别，可选值：" +
                    String.join(", ", TagCategory.getAllValues()));
        }

        // 3. 如果修改了名称或类别，检查是否与其他标签冲突
        if (StringUtils.hasText(request.getName()) || StringUtils.hasText(request.getCategory())) {
            String newName = StringUtils.hasText(request.getName())
                    ? request.getName()
                    : existingTag.getName();

            String newCategory = StringUtils.hasText(request.getCategory())
                    ? request.getCategory()
                    : existingTag.getCategory();

            LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Tag::getName, newName)
                    .eq(Tag::getCategory, newCategory)
                    .ne(Tag::getId, id);

            long count = this.count(wrapper);
            if (count > 0) {
                // 使用业务异常返回 400，表示请求参数导致的名称冲突
                throw new BusinessException(400, "标签名称已存在");
            }
        }

        // 4. 更新字段（只更新有值的字段）
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

        // 调用 MyBatis Plus 的按主键更新方法，并检查是否实际更新到记录
        boolean updated = this.updateById(tag);
        if (updated) {
            log.info("标签修改成功，ID：{}", id);
        } else {
            // 这里一般表示在并发删除/修改或逻辑删除等场景下，未能成功更新任何记录
            log.warn("标签修改失败，未更新任何记录，ID：{}", id);
            // 使用业务异常返回 500，表示标签更新业务处理失败
            throw new BusinessException(500, "标签修改失败，可能是标签已被删除或发生并发修改");
        }
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

        Tag existingTag = this.getById(id);
        if (existingTag == null) {
            // 使用业务异常返回 404 语义，避免被兜底异常处理成 500
            throw new BusinessException(404, "标签不存在，ID：" + id);
        }

        boolean removed = this.removeById(id);

        if (removed) {
            log.info("标签删除成功，ID：{}", id);
        } else {
            log.warn("标签删除失败，ID：{}", id);
            // 删除失败视为服务器内部错误，使用 500 业务码
            throw new BusinessException(500, "标签删除失败，ID：" + id);
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
            // 与删除接口保持一致，使用业务异常表达资源不存在（404）
            throw new BusinessException(404, "标签不存在，ID：" + id);
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
     * @Return: List<ServiceTagResponse> 服务标签列表
     * @Description: 获取所有类别为'service'的标签，用于服务商的服务类型多选
    **/
    @Override
    public List<ServiceTagResponse> getServiceTags() {
        log.info("查询服务类型标签列表");

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getCategory, TagCategory.SERVICE.getValue())
                .orderByAsc(Tag::getName);

        List<Tag> tags = this.list(wrapper);

        return tags.stream()
                .map(tag -> new ServiceTagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getCategory()
                ))
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:05
     * @Param: 
     * @Return: List<ServiceTagResponse> 认证类型标签列表
     * @Description: 获取所有类别为'certification'的标签，用于证书类型选择
    **/
    @Override
    public List<ServiceTagResponse> getCertificationTags() {
        log.info("查询认证类型标签列表");

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getCategory, TagCategory.CERTIFICATION.getValue())
                .orderByAsc(Tag::getName);

        return this.list(wrapper).stream()
                .map(tag -> new ServiceTagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getCategory()
                ))
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Param: 
     * @Return: List<ServiceTagResponse> 产品类型标签列表
     * @Description: 获取所有类别为'product'的标签，用于产品类型选择
    **/
    @Override
    public List<ServiceTagResponse> getProductTags() {
        log.info("查询产品类型标签列表");

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getCategory, TagCategory.PRODUCT.getValue())
                .orderByAsc(Tag::getName);

        return this.list(wrapper).stream()
                .map(tag -> new ServiceTagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getCategory()
                ))
                .collect(Collectors.toList());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/14 01:06
     * @Return: List<ServiceTagResponse> 其他类型标签列表
     * @Description: 获取所有类别为'general'的标签，用于通用标签选择
     **/
    @Override
    public List<ServiceTagResponse> getGeneralTags() {
        log.info("查询其他类型标签列表");
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getCategory, TagCategory.GENERAL.getValue())
                .orderByAsc(Tag::getName);
        return this.list(wrapper).stream()
                .map(tag -> new ServiceTagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getCategory()
                ))
                .collect(Collectors.toList());
    }
}
