package com.ikrai.likemain.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikrai.likemain.config.RedisConfig;
import com.ikrai.likemain.constant.ThumbConstant;
import com.ikrai.likemain.model.entity.Thumb;
import com.ikrai.likemain.model.entity.User;
import com.ikrai.likemain.model.vo.BlogVO;
import com.ikrai.likemain.service.BlogService;
import com.ikrai.likemain.mapper.BlogMapper;
import com.ikrai.likemain.model.entity.Blog;
import com.ikrai.likemain.service.ThumbService;
import com.ikrai.likemain.service.UserService;
import com.ikrai.likemain.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 博客服务实现类
 *
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {
    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private ThumbService thumbService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog, loginUser);
    }

    private BlogVO getBlogVO(Blog blog, User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog, blogVO);

        if (loginUser == null) {
            return blogVO;
        }

        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogVO.setHasThumb(exist);

        return blogVO;
    }
@Override
    public java.util.List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        HashMap<Long, Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjUtil.isNotEmpty(loginUser)) {
            List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            // 获取点赞
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(RedisKeyUtil.getUserThumbKey(loginUser.getId()), blogIdList);
        for (int i = 0; i < thumbList.size(); i++) {
            if (thumbList.get(i) == null) {
                continue;
            }
            blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
        }
    }
    return blogList.stream()
            .map(blog -> {
                BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                return blogVO;
            })
            .toList();
    }
}
