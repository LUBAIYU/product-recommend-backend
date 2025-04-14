package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品抓取请求体
 *
 * @author lzh
 */
@Data
public class ProductFetchDto implements Serializable {

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 商品名称前缀
     */
    private String namePrefix;

    private static final long serialVersionUID = 1L;
}
