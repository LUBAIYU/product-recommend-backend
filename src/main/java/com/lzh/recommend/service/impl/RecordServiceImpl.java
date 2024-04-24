package com.lzh.recommend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.mapper.RecordMapper;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author by
 */
@Service
@Slf4j
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record>
        implements RecordService {

    @Override
    public void addRecords(List<ProductVo> productVos, HttpServletRequest request) {
        //获取登录的用户ID
        Object object = request.getSession().getAttribute(UserConsts.USER_LOGIN_STATE);
        UserVo userVo = (UserVo) object;
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
                record.setCount(1);
                this.save(record);
                continue;
            }
            //如果记录存在则对count值+1
            dbRecord.setCount(dbRecord.getCount() + 1);
            //更新记录
            this.updateById(dbRecord);
        }
    }
}




