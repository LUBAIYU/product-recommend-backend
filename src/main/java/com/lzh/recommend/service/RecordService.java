package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.ProductVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author by
 */
public interface RecordService extends IService<Record> {

    /**
     * 存储用户搜索记录
     *
     * @param productVos 商品列表
     * @param request    请求体
     */
    void addRecords(List<ProductVo> productVos, HttpServletRequest request);

    /**
     * 加入购物车后添加分数
     *
     * @param productId 商品ID
     * @param userId    用户ID
     */
    void addScores(Long productId, Long userId);
}
