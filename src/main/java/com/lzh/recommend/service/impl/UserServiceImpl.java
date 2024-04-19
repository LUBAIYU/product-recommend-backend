package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.enums.RoleEnum;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.UserMapper;
import com.lzh.recommend.model.dto.LoginDto;
import com.lzh.recommend.model.dto.PageUserDto;
import com.lzh.recommend.model.dto.RegisterDto;
import com.lzh.recommend.model.dto.UserUpdateDto;
import com.lzh.recommend.model.entity.User;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author by
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Value("${product.recommend.path.domain}")
    private String domain;
    @Value("${product.recommend.path.address}")
    private String address;

    @Override
    public UserVo login(LoginDto loginDto, HttpServletRequest request) {
        //获取请求参数
        String userName = loginDto.getUserName();
        String userPassword = loginDto.getUserPassword();
        //判断请求参数是否为空
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断请求参数长度是否合法
        if (userName.length() < UserConsts.USER_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_NAME_ERROR);
        }
        if (userPassword.length() < UserConsts.USER_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_PASSWORD_ERROR);
        }
        //判断用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, userName);
        User user = this.getOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_PARAMS_ERROR);
        }
        //判断密码是否正确
        String encryptPassword = DigestUtil.md5Hex(userPassword + user.getSalt());
        if (!user.getUserPassword().equals(encryptPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_PARAMS_ERROR);
        }
        //用户信息脱敏
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        //设置用户登录态
        request.getSession().setAttribute(UserConsts.USER_LOGIN_STATE, userVo);
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_NAME_ERROR);
        }
        if (userPassword.length() < UserConsts.USER_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_PASSWORD_ERROR);
        }
        //判断确认密码和密码是否一致
        if (!userPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.PASSWORD_NOT_EQUAL);
        }
        //判断用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName, userName);
        User user = this.getOne(wrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_NAME_EXIST);
        }
        //生成一个随机的盐
        String salt = RandomUtil.randomString(4);
        //对密码进行加密
        String encryptPassword = DigestUtil.md5Hex(userPassword + salt);
        //插入用户数据
        user = new User();
        user.setUserName(userName);
        user.setUserPassword(encryptPassword);
        user.setRole(RoleEnum.USER.getCode());
        user.setSalt(salt);
        this.save(user);
    }

    @Override
    public UserVo getLoginUser(HttpServletRequest request) {
        //获取用户登录态
        Object object = request.getSession().getAttribute(UserConsts.USER_LOGIN_STATE);
        UserVo userVo = (UserVo) object;
        //判断是否为空
        if (userVo == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //返回结果
        return userVo;
    }

    @Override
    public String uploadImage(MultipartFile multipartFile) {
        //判断图片名称是否为空
        String originalFilename = multipartFile.getOriginalFilename();
        if (StrUtil.isBlank(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.IMAGE_UPLOAD_ERROR);
        }
        //判断图片后缀是否存在
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (StrUtil.isBlank(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.IMAGE_FORMAT_ERROR);
        }
        //生成随机文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        //上传图片
        File dest = new File(address + "/" + newFileName);
        try {
            multipartFile.transferTo(dest);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.IMAGE_UPLOAD_ERROR);
        }
        //获取并返回图片请求路径
        return domain + "/user/get/avatar/" + newFileName;
    }

    @Override
    public void getUserAvatar(String fileName, HttpServletResponse response) {
        //获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        //获取图片存放路径
        String url = address + "/" + fileName;
        //响应图片
        response.setContentType("image/" + suffix);
        //从服务器中读取图片
        try (
                //获取输出流
                OutputStream outputStream = response.getOutputStream();
                //获取输入流
                FileInputStream fileInputStream = new FileInputStream(url)
        ) {
            byte[] buffer = new byte[1024];
            int b;
            while ((b = fileInputStream.read(buffer)) != -1) {
                //将图片以字节流形式写入输出流
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.IMAGE_READ_ERROR);
        }
    }

    @Override
    public void updateInfo(UserUpdateDto userUpdateDto) {
        //获取请求参数
        Long id = userUpdateDto.getId();
        String userName = userUpdateDto.getUserName();
        String userAvatar = userUpdateDto.getUserAvatar();
        Integer gender = userUpdateDto.getGender();
        Integer age = userUpdateDto.getAge();
        String phone = userUpdateDto.getPhone();
        String address = userUpdateDto.getAddress();
        //校验参数是否合法
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);
        if (StrUtil.isNotBlank(userName)) {
            wrapper.set(User::getUserName, userName);
        }
        wrapper.set(User::getUserAvatar, userAvatar);
        wrapper.set(User::getAddress, address);
        //如果想要更新年龄，则值需为0或1
        if (gender != null) {
            if (gender < 0 || gender > 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.GENDER_PARAM_ERROR);
            }
        }
        wrapper.set(User::getGender, gender);
        //如果想要更新年龄，则值需为0到100
        if (age != null) {
            if (age < UserConsts.AGE_MIN || age > UserConsts.AGE_MAX) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.AGE_PARAM_ERROR);
            }
        }
        wrapper.set(User::getAge, age);
        //如果想要更新电话，则值需为11位数字
        if (StrUtil.isNotBlank(phone)) {
            if (phone.length() != UserConsts.PHONE_REQUIRED_LENGTH) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.PHONE_PARAM_ERROR);
            }
        }
        wrapper.set(User::getPhone, phone);
        //执行更新操作
        this.update(wrapper);
    }

    @Override
    public void delUserById(Long id) {
        //判断用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //存在则删除
        this.removeById(id);
    }

    @Override
    public PageBean<User> listUsersByPage(PageUserDto pageUserDto) {
        //获取分页参数
        Integer current = pageUserDto.getCurrent();
        Integer pageSize = pageUserDto.getPageSize();
        Long id = pageUserDto.getId();
        String userName = pageUserDto.getUserName();
        Integer gender = pageUserDto.getGender();
        //判断分页参数是否合法
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        //添加分页参数
        Page<User> page = new Page<>(current, pageSize);
        //添加条件参数
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(id != null, User::getId, id);
        wrapper.like(StrUtil.isNotBlank(userName), User::getUserName, userName);
        wrapper.eq(gender != null, User::getGender, gender);
        //查询
        this.page(page, wrapper);
        //返回记录
        return PageBean.of(page.getTotal(), page.getRecords());
    }

    @Override
    public UserVo getUserById(Long id) {
        //查询用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //封装返回对象
        UserVo userVo = new UserVo();
        BeanUtil.copyProperties(user, userVo);
        return userVo;
    }
}




