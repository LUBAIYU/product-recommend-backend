package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.entity.Cart;

import javax.servlet.http.HttpServletRequest;

/**
 * @author by
 */
public interface CartService extends IService<Cart> {

    /**
     * 添加购物车
     *
     * @param productId 商品ID
     * @param request   请求对象
     */
    void addCart(Long productId, HttpServletRequest request);
}
