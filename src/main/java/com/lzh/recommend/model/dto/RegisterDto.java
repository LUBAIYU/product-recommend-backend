package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 注册请求体
 *
 * @author by
 */
@Data
public class RegisterDto implements Serializable {
    private String userName;
    private String userPassword;
    private String confirmPassword;
}
