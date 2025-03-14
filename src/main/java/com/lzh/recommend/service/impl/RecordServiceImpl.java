package com.lzh.recommend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.enums.OpTypeEnum;
import com.lzh.recommend.mapper.RecordMapper;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.RecordService;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by
 */
@Service
@Slf4j
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record>
        implements RecordService {

    @Resource
    private UserService userService;

    @Override
    public void addRecords(List<ProductVo> productVos, HttpServletRequest request) {
        // 获取登录的用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();

        // 获取代理对象
        RecordService proxyService = (RecordService) AopContext.currentProxy();

        // 获取商品ID列表
        List<Long> productIdList = productVos.stream()
                .map(ProductVo::getId)
                .collect(Collectors.toList());

        // 获取已经存在的记录列表
        List<Record> recordList = this.lambdaQuery()
                .eq(Record::getUserId, userId)
                .in(Record::getProductId, productIdList)
                .list();

        // 如果记录存在则对分数加1，不存在则插入记录，并设置分数为1
        if (CollUtil.isNotEmpty(recordList)) {
            for (Record record : recordList) {
                record.setScore(record.getScore() + OpTypeEnum.PRODUCT_SEARCH.getScore());
                record.setUpdateTime(new Date());
            }
            // 批量更新
            proxyService.updateBatchById(recordList);
            // 取出记录存在的商品ID列表
            List<Long> existProductIdList = recordList.stream().map(Record::getProductId)
                    .collect(Collectors.toList());
            // 取出记录不存在的商品ID列表
            List<Long> notExistProductIdList = productIdList.stream()
                    .filter(productId -> !existProductIdList.contains(productId))
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(notExistProductIdList)) {
                // 批量插入不存在的记录
                batchInsertRecords(new ArrayList<>(), notExistProductIdList, userId, proxyService);
            }
        } else {
            // 批量插入所有记录
            batchInsertRecords(new ArrayList<>(), productIdList, userId, proxyService);
        }
    }

    @Override
    public void saveOrUpdate(Long userId, Long productId, OpTypeEnum opTypeEnum) {
        // 判断记录是否存在
        Record record = this.lambdaQuery()
                .eq(Record::getUserId, userId)
                .eq(Record::getProductId, productId)
                .one();
        // 插入记录
        if (ObjectUtil.isEmpty(record)) {
            record = new Record();
            record.setUserId(userId);
            record.setProductId(productId);
            record.setScore(opTypeEnum.getScore());
            boolean saved = this.save(record);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存记录失败");
            return;
        }
        // 更新记录
        record.setScore(record.getScore() + opTypeEnum.getScore());
        record.setUpdateTime(new Date());
        boolean updated = this.updateById(record);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "更新记录失败");
    }

    /**
     * 批量插入记录列表
     *
     * @param recordList    记录列表
     * @param productIdList 商品ID列表
     * @param userId        用户ID
     * @param recordService 代理对象
     */
    private void batchInsertRecords(List<Record> recordList, List<Long> productIdList, Long userId, RecordService recordService) {
        for (Long productId : productIdList) {
            Record record = new Record();
            record.setUserId(userId);
            record.setProductId(productId);
            record.setScore(OpTypeEnum.PRODUCT_SEARCH.getScore());
            recordList.add(record);
        }
        // 批量插入
        recordService.saveBatch(recordList);
    }
}




