package com.ikrai.likemain.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikrai.likemain.model.entity.Thumb;
import com.ikrai.likemain.model.entity.User;
import com.ikrai.likemain.model.vo.BlogVO;
import com.ikrai.likemain.service.BlogService;
import com.ikrai.likemain.mapper.BlogMapper;
import com.ikrai.likemain.model.entity.Blog;
import com.ikrai.likemain.service.ThumbService;
import com.ikrai.likemain.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
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

        Thumb thumb = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .eq(Thumb::getBlogId, blog.getId())
                .one();
        blogVO.setHasThumb(thumb != null);

        return blogVO;
    }
@Override
    public java.util.List<BlogVO> getBlogVOList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        HashMap<Long, Boolean> blogIdhasThumbMap = new HashMap<>();
    if (ObjUtil.isNotEmpty(blogList)) {
        Set<Long> blogIdSet = blogList.stream().map(Blog::getId).collect(Collectors.toSet());
        //获取点赞
        List<Thumb> thumbList = thumbService.lambdaQuery()
                .eq(Thumb::getUserId, loginUser.getId())
                .in(Thumb::getBlogId, blogIdSet)
                .list();
        thumbList.forEach(blogThumb -> blogIdhasThumbMap.put(blogThumb.getBlogId(), true));
    }
    return blogList.stream()
            .map(blog -> {
                BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
                blogVO.setHasThumb(blogIdhasThumbMap.get(blog.getId()));
                return blogVO;
            })
            .toList();
    }
}
