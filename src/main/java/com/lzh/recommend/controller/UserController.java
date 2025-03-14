package com.lzh.recommend.controller;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public Result<UserVo> userLogin(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        if (loginDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVo userVo = userService.login(loginDto, request);
        return Result.success(userVo);
    }

    @PostMapping("/register")
    public Result<Boolean> userRegister(@RequestBody RegisterDto registerDto) {
        if (registerDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.register(registerDto);
        return Result.success(true);
    }

    @GetMapping("/get/loginUser")
    public Result<UserVo> getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVo userVo = userService.getLoginUser(request);
        return Result.success(userVo);
    }

    @PostMapping("/avatar/upload")
    @LoginCheck
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String imageUrl = userService.uploadImage(multipartFile, request);
        return Result.success(imageUrl);
    }

    @PostMapping("/logout")
    @LoginCheck
    public Result<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //移除登录态
        request.getSession().setAttribute(UserConsts.USER_LOGIN_STATE, null);
        return Result.success(true);
    }

    @PutMapping("/update")
    @LoginCheck
    public Result<Boolean> updateUserInfo(@RequestBody UserUpdateDto userUpdateDto, HttpServletRequest request) {
        if (userUpdateDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updateInfo(userUpdateDto, request);
        return Result.success(true);
    }

    @DeleteMapping("/delete/{id}")
    @MustAdmin
    public Result<Boolean> deleteUserById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.delUserById(id);
        return Result.success(true);
    }

    @PostMapping("/page")
    @MustAdmin
    public Result<PageBean<User>> listUsersByPage(@RequestBody PageUserDto pageUserDto) {
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
