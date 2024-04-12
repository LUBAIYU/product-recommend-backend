package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.UserMapper;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author by
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public UserVo login(LoginDto loginDto) {
        //获取请求参数
        String userName = loginDto.getUserName();
        String userPassword = loginDto.getUserPassword();
        //判断请求参数是否为空
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断请求参数长度是否合法
        if (userName.length() < UserConsts.USER_NAME_LENGTH) {
            throw new BusinessException(40000, UserConsts.USER_NAME_ERROR);
        }
        if (userPassword.length() < UserConsts.USER_PASSWORD_LENGTH) {
            throw new BusinessException(40000, UserConsts.USER_PASSWORD_ERROR);
        }
        //判断用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, userName);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException(40000, UserConsts.USER_PARAMS_ERROR);
        }
        //判断密码是否正确
        String encryptPassword = DigestUtil.md5Hex(userPassword + user.getSalt());
        if (!user.getUserPassword().equals(encryptPassword)) {
            throw new BusinessException(40000, UserConsts.USER_PARAMS_ERROR);
        }
        //用户信息脱敏
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        //返回
        return userVo;
    }

    @Override
    public void register(RegisterDto registerDto) {
        //获取请求参数
        String userName = registerDto.getUserName();
        String userPassword = registerDto.getUserPassword();
        String confirmPassword = registerDto.getConfirmPassword();
        //判断参数是否为空
        if (StringUtils.isAnyBlank(userName, userPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断参数长度是否合法
        if (userName.length() < UserConsts.USER_NAME_LENGTH) {
            throw new BusinessException(40000, UserConsts.USER_NAME_ERROR);
        }
        if (userPassword.length() < UserConsts.USER_PASSWORD_LENGTH) {
            throw new BusinessException(40000, UserConsts.USER_PASSWORD_ERROR);
        }
        //判断确认密码和密码是否一致
        if (!userPassword.equals(confirmPassword)) {
            throw new BusinessException(40000, UserConsts.PASSWORD_NOT_EQUAL);
        }
        //判断用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, userName);
        User user = this.getOne(wrapper);
        if (user != null) {
            throw new BusinessException(40000, UserConsts.USER_NAME_EXIST);
        }
        //生成一个随机的盐
        String salt = RandomUtil.randomString(4);
        //对密码进行加密
        String encryptPassword = DigestUtil.md5Hex(userPassword + salt);
        //插入用户数据
        user = new User();
        user.setUserName(userName);
        user.setUserPassword(encryptPassword);
        user.setSalt(salt);
        this.save(user);
    }
}




