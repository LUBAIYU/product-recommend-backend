package com.lzh.recommend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品表
 *
 * @author by
 */
@TableName(value = "product")
@Data
public class Product implements Serializable {
    /**
     * 商品ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（0-未删除，1-删除）
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}