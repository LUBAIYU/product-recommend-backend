package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author by
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param loginDto 登录请求体
     * @param request  请求
     * @return 返回用户脱敏信息
     */
    UserVo login(LoginDto loginDto, HttpServletRequest request);

    /**
     * 用户注册
     *
     * @param registerDto 注册请求体
     */
    void register(RegisterDto registerDto);

    /**
     * 获取当前登录的用户信息
     *
     * @param request 请求
     */
    UserVo getLoginUser(HttpServletRequest request);
}
