package com.ikrai.likemain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ikrai.likemain.mapper.UserMapper;
import com.ikrai.likemain.model.entity.User;

import com.ikrai.likemain.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}