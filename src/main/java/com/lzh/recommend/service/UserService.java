package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.PageUserDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.dto.UserUpdateDto;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.utils.PageBean;
import org.springframework.web.multipart.MultipartFile;

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
     * @return 返回脱敏后的用户信息
     */
    UserVo getLoginUser(HttpServletRequest request);

    /**
     * 上传图片
     *
     * @param multipartFile 文件上传对象
     * @param request       请求对象
     * @return 文件路径
     */
    String uploadImage(MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 修改用户信息
     *
     * @param userUpdateDto 用户信息修改对象
     */
    void updateInfo(UserUpdateDto userUpdateDto);

    /**
     * 根据ID删除用户信息
     *
     * @param id 用户ID
     */
    void delUserById(Long id);

    /**
     * 条件分页查询用户信息
     *
     * @param pageUserDto 分页查询请求体
     * @return 分页结果
     */
    PageBean<User> listUsersByPage(PageUserDto pageUserDto);

    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserVo getUserById(Long id);
}
