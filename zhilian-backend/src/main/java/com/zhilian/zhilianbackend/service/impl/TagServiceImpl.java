package com.zhilian.zhilianbackend.service.impl;

import com.zhilian.zhilianbackend.entity.Tag;
import com.zhilian.zhilianbackend.mapper.TagMapper;
import com.zhilian.zhilianbackend.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: 6017
 * @Date: 2026/3/9 20:49
 * @Param:
 * @Return: 
 * @Description: 标签字典表业务逻辑实现类，实现标签相关的业务方法
**/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}
