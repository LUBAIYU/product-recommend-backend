package com.lzh.recommend.controller;

import cn.hutool.core.util.StrUtil;
import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.annotation.MustAdmin;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.PageUserDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.dto.UserUpdateDto;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @Value("${product.recommend.path.user-prefix}")
    private String prefix;

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
    @LoginCheck
    public Result<String> uploadAvatar(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String imageUrl = userService.uploadImage(multipartFile, prefix);
        return Result.success(imageUrl);
    }

    @GetMapping("/get/avatar/{fileName}")
    @LoginCheck
    public void getAvatar(@PathVariable String fileName, HttpServletResponse response) {
        if (StrUtil.isBlank(fileName) || response == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getImage(fileName, response);
    }

    @PostMapping("/logout")
    @LoginCheck
    public Result<Void> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //移除登录态
        request.getSession().setAttribute(UserConsts.USER_LOGIN_STATE, null);
        return Result.success();
    }

    @PostMapping("/update/info")
    @LoginCheck
    public Result<Void> updateInfo(@RequestBody UserUpdateDto userUpdateDto) {
        if (userUpdateDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updateInfo(userUpdateDto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @MustAdmin
    public Result<Void> delUserInfo(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.delUserById(id);
        return Result.success();
    }

    @GetMapping("/page")
    @MustAdmin
    public Result<PageBean<User>> listUsersByPage(PageUserDto pageUserDto) {
        if (pageUserDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<User> pageBean = userService.listUsersByPage(pageUserDto);
        return Result.success(pageBean);
    }

    @GetMapping("/get/{id}")
    @MustAdmin
    public Result<UserVo> getUserById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVo userVo = userService.getUserById(id);
        return Result.success(userVo);
    }
}
