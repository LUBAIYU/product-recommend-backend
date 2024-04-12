package com.lzh.recommend.service;

import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.vo.UserVo;

/**
 * @author by
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDto 登录请求体
     * @return 返回用户脱敏信息
     */
    UserVo login(LoginDto loginDto);

    /**
     * 用户注册
     *
     * @param registerDto 注册请求体
     */
    void register(RegisterDto registerDto);
}
