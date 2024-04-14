package com.lzh.recommend.controller;

import cn.hutool.core.util.StrUtil;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @GetMapping("/get/loginUser")
    public Result<UserVo> getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVo userVo = userService.getLoginUser(request);
        return Result.success(userVo);
    }

    @PostMapping("/upload/avatar")
    public Result<String> uploadImage(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String imageUrl = userService.uploadImage(multipartFile);
        return Result.success(imageUrl);
    }

    @GetMapping("/get/avatar/{fileName}")
    public void getUserAvatar(@PathVariable String fileName, HttpServletResponse response) {
        if (StrUtil.isBlank(fileName) || response == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getUserAvatar(fileName, response);
    }
}
