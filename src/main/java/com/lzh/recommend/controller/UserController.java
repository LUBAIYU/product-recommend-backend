package com.lzh.recommend.controller;

import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author by
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<UserVo> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        if (loginDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVo userVo = userService.login(loginDto, request);
        return Result.success(userVo);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDto registerDto) {
        if (registerDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.register(registerDto);
        return Result.success();
    }
}
