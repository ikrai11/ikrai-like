package com.ikrai.likemain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikrai.likemain.service.BlogService;
import com.ikrai.likemain.mapper.BlogMapper;
import com.ikrai.likemain.model.entity.Blog;
import org.springframework.stereotype.Service;

/**
 * 博客服务实现类
 *
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

}
