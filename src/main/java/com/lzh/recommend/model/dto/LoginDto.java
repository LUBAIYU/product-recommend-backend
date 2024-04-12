package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author by
 */
@Data
public class LoginDto implements Serializable {
    private String userName;
    private String userPassword;
}
