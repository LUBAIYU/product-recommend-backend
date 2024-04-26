package com.lzh.recommend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 购物车信息返回类
 *
 * @author by
 */
@Data
public class CartVo {
    /**
     * 购物车信息ID
     */
    private Long cartId;

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
     * 购买数量
     */
    private Integer num;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
