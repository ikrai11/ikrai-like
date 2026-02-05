package com.ikrai.likemain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ikrai.likemain.service.ThumbService;
import com.ikrai.likemain.mapper.ThumbMapper;
import com.ikrai.likemain.model.entity.Thumb;
import org.springframework.stereotype.Service;

/**
 * 点赞服务实现类
 *
 */
@Service
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb>
        implements ThumbService {

}