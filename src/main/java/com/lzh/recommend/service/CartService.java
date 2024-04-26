package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.dto.PageDto;
import com.lzh.recommend.model.entity.Cart;
import com.lzh.recommend.model.vo.CartVo;
import com.lzh.recommend.utils.PageBean;

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

    /**
     * 删除购物车
     *
     * @param cartId  单条购物车数据ID
     * @param request 请求对象
     */
    void deleteCart(Long cartId, HttpServletRequest request);

    /**
     * 分页查询购物车数据
     *
     * @param pageDto 分页对象
     * @param request 请求对象
     * @return 分页数据
     */
    PageBean<CartVo> pageCartInfos(PageDto pageDto, HttpServletRequest request);
}
