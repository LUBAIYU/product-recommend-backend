package com.lzh.recommend.enums;

import lombok.Getter;

/**
 * 操作类型枚举类
 *
 * @author by
 */
@Getter
public enum OpTypeEnum {

    /**
     * 操作类型枚举
     */
    PRODUCT_SEARCH("搜索商品", 1),

    PRODUCT_DETAIL("查看商品", 2),

    ADD_CART("加入购物车", 3),

    PRODUCT_PURCHASE("购买商品", 4);

    private final String text;

    private final Integer score;

    OpTypeEnum(String text, Integer score) {
        this.text = text;
        this.score = score;
    }
}
