package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求体
 *
 * @author by
 */
@Data
public class PageProductDto implements Serializable {
    /**
     * 当前页码
     */
    private Integer current;
    /**
     * 每页记录数
     */
    private Integer pageSize;
    /**
     * 商品ID
     */
    private Long id;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 状态（0-上架，1-下架）
     */
    private Integer status;
}
