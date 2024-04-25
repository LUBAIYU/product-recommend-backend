package com.lzh.recommend.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分页请求体
 *
 * @author by
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageProductDto extends PageDto implements Serializable {
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
