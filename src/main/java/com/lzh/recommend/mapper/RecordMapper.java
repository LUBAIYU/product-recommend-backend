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
     * 根据用户ID计算用户打分商品的总分
     *
     * @param userId 用户ID
     * @return 总分
     */
    @Select("select sum(score) from `product-recommend`.record where user_id = #{userId}")
    double sumScore(Long userId);
}




