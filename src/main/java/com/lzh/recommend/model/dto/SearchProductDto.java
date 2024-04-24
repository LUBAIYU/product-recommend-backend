package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author by
 */
@Data
public class SearchProductDto implements Serializable {
    /**
     * 当前页码
     */
    private Integer current;
    /**
     * 每页记录数
     */
    private Integer pageSize;
    /**
     * 商品名称
     */
    private String name;
}
