package com.zhilian.zhilianbackend.service.impl;

import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.mapper.TagMapper;
import com.zhilian.zhilianbackend.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签字典表 服务实现类
 * </p>
 *
 * @author 智链团队
 * @since 2026-03-08
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
