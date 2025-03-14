package com.lzh.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lzh.recommend.model.entity.SaleRecord;

/**
 * @author lzh
 */
public interface SaleRecordService extends IService<SaleRecord> {

    /**
     * 保存或更新销量记录
     *
     * @param productId   商品ID
     * @param purchaseNum 购买数量
     */
    void saveOrUpdate(Long productId, Integer purchaseNum);
}
