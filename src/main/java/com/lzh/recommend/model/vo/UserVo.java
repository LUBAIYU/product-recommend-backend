package com.lzh.recommend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户信息脱敏
 *
 * @author by
 */
@Data
public class UserVo {
    /**
     * 用户ID
     */
    private Long id;

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
     * 角色（0-管理员，1-普通用户）
     */
    private Integer role;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
