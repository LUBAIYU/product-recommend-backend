package com.lzh.recommend.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author lzh
 */
@Getter
public enum ProductStatusEnum {

    /**
     * 枚举值
     */
    ON_SALE(0, "上架"),
    OFF_SALE(1, "下架");

    private final Integer code;

    private final String message;

    ProductStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据code获取枚举
     *
     * @param code code
     * @return 枚举
     */
    public static ProductStatusEnum getEnumByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatusEnum statusEnum : ProductStatusEnum.values()) {
            if (statusEnum.code.equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
