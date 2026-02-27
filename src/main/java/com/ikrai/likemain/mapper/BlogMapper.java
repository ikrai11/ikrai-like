package com.ikrai.likemain.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ikrai.likemain.model.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 博客数据库操作Mapper
 *
 */
public interface BlogMapper extends BaseMapper<Blog> {
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);
}




