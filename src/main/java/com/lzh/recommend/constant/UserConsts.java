package com.lzh.recommend.constant;

/**
 * 用户常量类
 *
 * @author by
 */
public interface UserConsts {
    /**
     * 用户登录态
     */
    String USER_LOGIN_STATE = "user_login_state";
    Integer USER_ACCOUNT_LENGTH = 4;
    Integer USER_PASSWORD_LENGTH = 8;
    String USER_NAME_ERROR = "用户名长度不能小于4位！";
    String USER_PASSWORD_ERROR = "密码长度不能小于8位！";
    String USER_PARAMS_ERROR = "账号或密码错误！";
    String PASSWORD_NOT_EQUAL = "密码不一致！";
    String USER_NAME_EXIST = "用户名已经存在！";
    String GENDER_PARAM_ERROR = "性别参数异常！";
    String AGE_PARAM_ERROR = "年龄需是0到100之间！";
    Integer AGE_MIN = 0;
    Integer AGE_MAX = 100;
    Integer PHONE_REQUIRED_LENGTH = 11;
    String PHONE_PARAM_ERROR = "手机号长度需为11位！";

    /**
     * 默认性别
     */
    int DEFAULT_GENDER = 1;

    /**
     * 默认年龄
     */
    int DEFAULT_AGE = 25;

    /**
     * 年龄加权权重
     */
    double AGE_WEIGHT = 0.5;

    /**
     * 性别加权权重
     */
    double GENDER_WEIGHT = 0.5;
}
