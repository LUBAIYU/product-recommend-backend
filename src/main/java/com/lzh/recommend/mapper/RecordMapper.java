package com.lzh.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzh.recommend.model.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author by
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * 根据用户ID计算用户打分商品的平均分
     *
     * @param userId 用户ID
     * @return 平均分 保留小数点后两位
     */
    @Select("select round(avg(score),2) from `product-recommend`.record where user_id = #{userId}")
    Double avgScore(Long userId);
}




