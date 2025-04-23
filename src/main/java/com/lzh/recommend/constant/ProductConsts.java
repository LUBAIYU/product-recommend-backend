package com.lzh.recommend.constant;

/**
 * @author by
 */
public interface ProductConsts {
    String PRICE_PARAM_ERROR = "价格参数异常！";
    String STOCK_PARAM_ERROR = "库存参数异常！";
    String STATUS_PARAM_ERROR = "状态参数异常！";
    String DELETE_PRODUCT_ERROR = "商品上架中，不能删除！";

    /**
     * 最大权值
     */
    double MAX_POWER = 1.0;

    /**
     * 基础权值
     */
    double BASIC = 0.3;

    /**
     * 增长权值
     */
    double GROWTH_RATE = 0.07;
}
