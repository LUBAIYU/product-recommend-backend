package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页参数请求体
 *
 * @author by
 */
@Data
public class PageUserDto implements Serializable {
    /**
     * 当前页码
     */
    private Integer current;
    /**
     * 每页记录数
     */
    private Integer pageSize;
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 性别
     */
    private Integer gender;
}
