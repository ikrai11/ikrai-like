package com.ikrai.likemain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.ikrai.likemain.constant.ThumbConstant;
import com.ikrai.likemain.model.dto.thumb.DoThumbRequest;
import com.ikrai.likemain.model.entity.Blog;
import com.ikrai.likemain.model.entity.User;
import com.ikrai.likemain.service.BlogService;
import com.ikrai.likemain.service.ThumbService;
import com.ikrai.likemain.mapper.ThumbMapper;
import com.ikrai.likemain.model.entity.Thumb;
import com.ikrai.likemain.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import cn.hutool.json.JSONUtil;
import cn.hutool.core.map.MapUtil;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 点赞服务实现类
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    // 在doThumb方法前添加
    /**
     * 获取博客创建时间（先查Redis，再查数据库）
     */
    private long getBlogCreateTime(Long blogId) {
        // Redis中存储博客创建时间的key
        String createTimeKey = "blog:createTime:" + blogId;

        // 先从Redis获取
        Object createTimeObj = redisTemplate.opsForValue().get(createTimeKey);
        if (createTimeObj != null) {
            return Long.parseLong(createTimeObj.toString());
        }

        // Redis中不存在，从数据库获取
        Blog blog = blogService.getById(blogId);
        if (blog == null) {
            throw new RuntimeException("博客不存在");
        }

        long createTime = blog.getCreateTime().getTime();

        // 存储到Redis，设置较长过期时间（如30天）
        redisTemplate.opsForValue().set(createTimeKey, String.valueOf(createTime), 30, java.util.concurrent.TimeUnit.DAYS);

        return createTime;
    }

    /**
     * 判断是否为热数据（发布时间 <= 1个月）
     */
    private boolean isHotData(Long blogId) {
        long createTime = getBlogCreateTime(blogId);
        long oneMonthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        return createTime >= oneMonthAgo;
    }

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                boolean exists = this.hasThumb(blogId, loginUser.getId());
                if (exists) {
                    throw new RuntimeException("用户已点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                // 更新成功才执行
                Boolean success = update && this.save(thumb);
                //点赞记录存入Redis
                if (success) {
                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),
                            blogId.toString(), thumb.getId());
                }
                return success;
            });
        }
    }

    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Object thumbIdObj = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                if (thumbIdObj == null) {
                    throw new RuntimeException("用户未点赞");
                }
                Long thumbId = Long.valueOf(thumbIdObj.toString());

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean success = update && this.removeById(thumbId);
                // 点赞记录从 Redis 删除
                if (success) {
                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                }
                return success;
            });
        }
    }

    @Override
    public boolean hasThumb(Long blogId, Long userId) {

        String key = ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
        return redisTemplate.opsForHash().hasKey(key, blogId.toString());
    }

}
