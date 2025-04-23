package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.AgeGroupEnum;
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
import com.lzh.recommend.upload.FilePictureUpload;
import com.lzh.recommend.utils.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Value("${product.recommend.path.user-avatar-prefix}")
    private String userAvatarPrefix;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Override
    public UserVo login(LoginDto loginDto, HttpServletRequest request) {
        //获取请求参数
        String userAccount = loginDto.getUserAccount();
        String userPassword = loginDto.getUserPassword();
        //判断请求参数是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断请求参数长度是否合法
        if (userAccount.length() < UserConsts.USER_ACCOUNT_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_NAME_ERROR);
        }
        if (userPassword.length() < UserConsts.USER_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_PASSWORD_ERROR);
        }
        //判断用户是否存在
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .one();
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
        String userAccount = registerDto.getUserAccount();
        String userPassword = registerDto.getUserPassword();
        String confirmPassword = registerDto.getConfirmPassword();
        //判断参数是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断参数长度是否合法
        if (userAccount.length() < UserConsts.USER_ACCOUNT_LENGTH) {
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
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .one();
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, UserConsts.USER_NAME_EXIST);
        }
        //生成一个随机的盐
        String salt = RandomUtil.randomString(4);
        //对密码进行加密
        String encryptPassword = DigestUtil.md5Hex(userPassword + salt);
        //插入用户数据
        user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setRole(RoleEnum.USER.getCode());
        user.setSalt(salt);
        user.setGender(UserConsts.DEFAULT_GENDER);
        user.setAge(UserConsts.DEFAULT_AGE);
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
    public String uploadImage(MultipartFile multipartFile, HttpServletRequest request) {
        // 获取登录用户
        UserVo loginUser = this.getLoginUser(request);
        // 构造图片路径前缀
        String uploadPathPrefix = String.format("%s/%s", userAvatarPrefix, loginUser.getId());
        // 上传图片
        return filePictureUpload.uploadPicture(multipartFile, uploadPathPrefix);
    }

    @Override
    public void updateInfo(UserUpdateDto userUpdateDto, HttpServletRequest request) {
        //获取请求参数
        Long id = userUpdateDto.getId();
        String userAccount = userUpdateDto.getUserAccount();
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
        if (StrUtil.isNotBlank(userAccount)) {
            wrapper.set(User::getUserAccount, userAccount);
        }
        wrapper.set(User::getUserName, userName);
        wrapper.set(User::getUserAvatar, userAvatar);
        wrapper.set(User::getAddress, address);
        //如果想要更新性别，则值需为0或1
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

        // 如果登录用户更新了自己的信息，则更新用户登录态
        UserVo loginUser = this.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (loginUserId.equals(id)) {
            User user = this.getById(id);
            UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);
            request.getSession().setAttribute(UserConsts.USER_LOGIN_STATE, userVo);
        }
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
        String userAccount = pageUserDto.getUserAccount();
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
        wrapper.like(StrUtil.isNotBlank(userAccount), User::getUserAccount, userAccount);
        wrapper.like(StrUtil.isNotBlank(userName), User::getUserName, userName);
        wrapper.eq(gender != null, User::getGender, gender);
        wrapper.orderByDesc(User::getCreateTime);
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

    @Override
    public Map<Long, Double> getSimilarityMapByUserProperty(HttpServletRequest request) {
        // 获取当前登录用户ID、性别、年龄
        UserVo loginUser = this.getLoginUser(request);
        if (!hasValidAttributes(loginUser)) {
            return Collections.emptyMap();
        }

        // 判断注册用户是否只有登录用户
        List<User> userList = getUserListWithDefaults();
        if (userList.size() <= 1) {
            return Collections.emptyMap();
        }

        // 用于保存相似度集合
        Map<Long, Double> similarityMap = new HashMap<>((userList.size() - 1) << 1);
        // 计算相似度
        for (User user : userList) {
            if (user.getId().equals(loginUser.getId())) {
                continue;
            }
            double similarity = calculateAttributeSimilarity(
                    loginUser.getAge(), loginUser.getGender(),
                    user.getAge(), user.getGender()
            );
            similarityMap.put(user.getId(), similarity);
        }
        return similarityMap;
    }

    @Override
    public double calculateAttributeSimilarity(int loginUserAge, int loginUserGender, int otherUserAge, int otherUserGender) {
        // 计算年龄相似度
        double ageSimilarity = AgeGroupEnum.isSameGroup(loginUserAge, otherUserAge) ? 1 : 0;
        // 计算性别相似度
        double genderSimilarity = otherUserGender == loginUserGender ? 1 : 0;
        // 获取最终相似度
        return UserConsts.AGE_WEIGHT * ageSimilarity + UserConsts.GENDER_WEIGHT * genderSimilarity;
    }

    /**
     * @return 获取带有默认值的用户列表
     */
    private List<User> getUserListWithDefaults() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "gender", "age");
        List<User> userList = this.list(queryWrapper);
        // 统一补全默认值
        for (User user : userList) {
            if (ObjectUtil.isNull(user.getAge())) {
                user.setAge(UserConsts.DEFAULT_AGE);
            }
            if (ObjectUtil.isNull(user.getGender())) {
                user.setGender(UserConsts.DEFAULT_GENDER);
            }
        }
        return userList;
    }

    /**
     * 校验属性
     *
     * @param user 登录用户
     * @return 是否通过
     */
    private boolean hasValidAttributes(UserVo user) {
        return ObjectUtil.isNotNull(user.getGender()) && ObjectUtil.isNotNull(user.getAge());
    }
}




