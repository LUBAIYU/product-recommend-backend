package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息修改请求体
 *
 * @author by
 */
@Data
public class UserUpdateDto implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别（0-男，1-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 收货地址
     */
    private String address;
}
