package com.lzh.recommend.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author by
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchProductDto extends PageDto implements Serializable {
    /**
     * 商品名称
     */
    private String name;
}
