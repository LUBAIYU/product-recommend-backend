package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.enums.OpTypeEnum;
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
     * 添加或更新记录
     *
     * @param userId     用户ID
     * @param productId  商品ID
     * @param opTypeEnum 操作类型
     */
    void saveOrUpdate(Long userId, Long productId, OpTypeEnum opTypeEnum);
}
