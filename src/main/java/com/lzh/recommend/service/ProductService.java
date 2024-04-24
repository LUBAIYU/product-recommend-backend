package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.dto.PageProductDto;
import com.lzh.recommend.model.dto.ProductAddDto;
import com.lzh.recommend.model.dto.ProductUpdateDto;
import com.lzh.recommend.model.dto.SearchProductDto;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.utils.PageBean;

import javax.servlet.http.HttpServletRequest;


/**
 * @author by
 */
public interface ProductService extends IService<Product> {

    /**
     * 新增商品
     *
     * @param productAddDto 新增商品请求体
     */
    void addProduct(ProductAddDto productAddDto);

    /**
     * 根据ID删除商品
     *
     * @param id 商品ID
     */
    void deleteById(Long id);

    /**
     * 根据ID更新商品
     *
     * @param productUpdateDto 更新商品请求体
     */
    void updateProduct(ProductUpdateDto productUpdateDto);

    /**
     * 条件分页查询
     *
     * @param pageProductDto 分页条件请求体
     * @return 查询结果
     */
    PageBean<Product> listProductsByPage(PageProductDto pageProductDto);

    /**
     * 修改商品状态
     *
     * @param id     商品ID
     * @param status 商品状态
     */
    void alterStatus(Long id, Integer status);

    /**
     * 根据商品名称搜索商品
     *
     * @param searchProductDto 信息包装类
     * @param request          请求对象
     * @return 返回搜索结果
     */
    PageBean<ProductVo> searchProducts(SearchProductDto searchProductDto, HttpServletRequest request);

    /**
     * 推荐商品
     *
     * @param request 请求对象
     * @return 推荐结果
     */
    PageBean<ProductVo> recommend(HttpServletRequest request);
}
