package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.dto.*;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.utils.PageBean;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author by
 */
public interface ProductService extends IService<Product> {

    /**
     * 新增商品
     *
     * @param productAddDto 新增商品请求体
     * @return 商品ID
     */
    long addProduct(ProductAddDto productAddDto);

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
     * @param count   推荐数量
     * @param request 请求对象
     * @return 推荐结果
     */
    List<ProductVo> recommend(Integer count, HttpServletRequest request);

    /**
     * 购买商品
     *
     * @param productId 商品ID
     * @param request   请求对象
     */
    void purchaseProduct(Long productId, HttpServletRequest request);

    /**
     * 热门商品推荐
     *
     * @param count 推荐条数
     * @return 商品列表
     */
    List<ProductVo> recommendHotProducts(Integer count);

    /**
     * 随机获取商品
     *
     * @param count 获取数量
     * @return 商品列表
     */
    List<ProductVo> randomProducts(Integer count);

    /**
     * 协同过滤算法
     *
     * @param loginUserId         登录用户ID
     * @param count               推荐商品数量
     * @param userIdProductIdsMap 用户ID和商品ID集合映射
     * @param request             请求对象
     * @return 商品列表
     */
    List<ProductVo> collaborativeFiltering(Long loginUserId, Integer count, Map<Long, Set<Long>> userIdProductIdsMap, HttpServletRequest request);

    /**
     * 计算相似度，获取行为相似度集合
     *
     * @param loginUserId           登录用户ID
     * @param userIdProductIdsMap   用户Id商品ID集合
     * @param loginUserProductIdSet 登录用户商品ID集合
     * @return 行为相似度集合
     */
    Map<Long, Double> getSimilarityMapByBehaviorRecord(Long loginUserId, Map<Long, Set<Long>> userIdProductIdsMap, Set<Long> loginUserProductIdSet);

    /**
     * 计算最终评分
     *
     * @param commonProductSet 共同商品集合
     * @param similarityMap    相似度集合
     * @param loginUserId      登录用户ID
     * @param similaritySum    相似度之和
     * @return 最终评分集合
     */
    Map<Long, Double> getFinalScoreMap(Set<Long> commonProductSet, Map<Long, Double> similarityMap, Long loginUserId, double similaritySum);

    /**
     * 上传图片
     *
     * @param multipartFile 文件
     * @param request       请求对象
     * @return 文件地址
     */
    String uploadImage(MultipartFile multipartFile, HttpServletRequest request);

    /**
     * 获取商品详情
     *
     * @param id      商品ID
     * @param request 请求对象
     * @return 商品信息
     */
    ProductVo getProductVoById(Long id, HttpServletRequest request);

    /**
     * 批量添加商品
     *
     * @param productFetchDto 请求体
     * @param request         请求对象
     */
    void addProductByBatch(ProductFetchDto productFetchDto, HttpServletRequest request);
}
