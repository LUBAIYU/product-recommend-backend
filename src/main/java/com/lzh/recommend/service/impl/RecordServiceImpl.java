package com.lzh.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.enums.ScoreEnum;
import com.lzh.recommend.mapper.RecordMapper;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.RecordService;
import com.lzh.recommend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
        //获取登录的用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();
        //添加记录
        LambdaQueryWrapper<Record> wrapper;
        for (ProductVo productVo : productVos) {
            Long productId = productVo.getId();
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Record::getProductId, productId);
            wrapper.eq(Record::getUserId, userId);
            //根据用户ID和商品ID查询数据
            Record dbRecord = this.getOne(wrapper);
            if (dbRecord == null) {
                //如果记录不存在则插入记录
                Record record = new Record();
                record.setUserId(userId);
                record.setProductId(productId);
                record.setScore(ScoreEnum.ONE.getScore());
                this.save(record);
                continue;
            }
            //如果记录存在则对count值+1
            dbRecord.setScore(dbRecord.getScore() + ScoreEnum.ONE.getScore());
            //更新记录
            this.updateById(dbRecord);
        }
    }

    @Override
    public void addScores(Long productId, Long userId) {
        //查询记录是否存在
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getProductId, productId);
        Record dbRecord = this.getOne(wrapper);
        //如果记录为空则插入记录
        if (dbRecord == null) {
            Record record = new Record();
            record.setUserId(userId);
            record.setProductId(productId);
            record.setScore(ScoreEnum.TWO.getScore());
            //插入记录
            this.save(record);
            return;
        }
        //记录存在则更新分数，分数加2
        dbRecord.setScore(dbRecord.getScore() + ScoreEnum.TWO.getScore());
        this.updateById(dbRecord);
    }
}




