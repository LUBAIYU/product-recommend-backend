package com.lzh.recommend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.SaleRecordMapper;
import com.lzh.recommend.model.entity.SaleRecord;
import com.lzh.recommend.service.SaleRecordService;
import com.lzh.recommend.utils.ThrowUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author lzh
 */
@Service
public class SaleRecordServiceImpl extends ServiceImpl<SaleRecordMapper, SaleRecord> implements SaleRecordService {

    @Override
    public void saveOrUpdate(Long productId, Integer purchaseNum) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取销量记录
        SaleRecord saleRecord = this.lambdaQuery()
                .eq(SaleRecord::getProductId, productId)
                .one();
        // 不存在则插入记录
        if (ObjectUtil.isEmpty(saleRecord)) {
            saleRecord = new SaleRecord();
            saleRecord.setProductId(productId);
            saleRecord.setSaleNum(purchaseNum);
            boolean saved = this.save(saleRecord);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存商品销售记录失败");
            return;
        }
        // 更新记录
        saleRecord.setSaleNum(saleRecord.getSaleNum() + purchaseNum);
        saleRecord.setUpdateTime(new Date());
        boolean updated = this.updateById(saleRecord);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "更新商品销售记录失败");
    }
}




