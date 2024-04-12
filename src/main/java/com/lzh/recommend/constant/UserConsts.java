package com.lzh.recommend.constant;

/**
 * 用户常量类
 *
 * @author by
 */
public interface UserConsts {
    Integer USER_NAME_LENGTH = 4;
    Integer USER_PASSWORD_LENGTH = 8;
    String USER_NAME_ERROR = "用户名长度不能小于4位！";
    String USER_PASSWORD_ERROR = "密码长度不能小于8位！";
    String USER_PARAMS_ERROR = "用户名或密码错误！";
    String PASSWORD_NOT_EQUAL = "密码不一致！";
    String USER_NAME_EXIST = "用户名已经存在！";
}
