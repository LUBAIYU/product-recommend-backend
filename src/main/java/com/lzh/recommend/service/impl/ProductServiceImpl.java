package com.lzh.recommend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.mapper.ProductMapper;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * @author by
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

}




