package com.lzh.recommend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author by
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




