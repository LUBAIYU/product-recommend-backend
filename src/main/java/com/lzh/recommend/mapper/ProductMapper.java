package com.lzh.recommend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lzh.recommend.model.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author by
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 随机从数据库中获取count条商品
     *
     * @param count
     * @return
     */
    @Select("select * from `product-recommend`.product where status = 0 order by rand() limit #{count}")
    List<Product> randomProducts(Integer count);
}




