package com.lzh.recommend.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页参数请求体
 *
 * @author by
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageUserDto extends PageDto implements Serializable {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 性别
     */
    private Integer gender;
}
