package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品新增请求体
 *
 * @author by
 */
@Data
public class ProductAddDto implements Serializable {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 价格
     */
    private Integer price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 状态（0-上架，1-下架）
     */
    private Integer status;
}
