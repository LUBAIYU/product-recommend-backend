package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.constant.ProductConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.ProductMapper;
import com.lzh.recommend.model.dto.PageProductDto;
import com.lzh.recommend.model.dto.ProductAddDto;
import com.lzh.recommend.model.dto.ProductUpdateDto;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.utils.PageBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author by
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    @Override
    public void addProduct(ProductAddDto productAddDto) {
        //获取请求参数
        String name = productAddDto.getName();
        String image = productAddDto.getImage();
        Integer price = productAddDto.getPrice();
        Integer stock = productAddDto.getStock();
        Integer status = productAddDto.getStatus();
        //判断参数是否符合要求
        if (StringUtils.isAnyBlank(name, image)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (price == null || price < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.PRICE_PARAM_ERROR);
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STOCK_PARAM_ERROR);
        }
        if (status != null) {
            if (status < 0 || status > 1) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STATUS_PARAM_ERROR);
            }
        }
        Product product = new Product();
        BeanUtil.copyProperties(productAddDto, product);
        //新增
        this.save(product);
    }

    @Override
    public void deleteById(Long id) {
        //判断商品是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断商品是否处于上架状态
        if (product.getStatus() == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.DELETE_PRODUCT_ERROR);
        }
        //删除商品
        this.removeById(id);
    }

    @Override
    public void updateProduct(ProductUpdateDto productUpdateDto) {
        //获取请求参数
        Long id = productUpdateDto.getId();
        String name = productUpdateDto.getName();
        String image = productUpdateDto.getImage();
        Integer price = productUpdateDto.getPrice();
        Integer stock = productUpdateDto.getStock();
        //判断参数是否合法
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isAnyBlank(name, image)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (price == null || price < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.PRICE_PARAM_ERROR);
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, ProductConsts.STOCK_PARAM_ERROR);
        }
        //创建更新请求体
        Product product = new Product();
        BeanUtil.copyProperties(productUpdateDto, product);
        //更新
        this.updateById(product);
    }

    @Override
    public PageBean<Product> listProductsByPage(PageProductDto pageProductDto) {
        //获取请求参数
        Integer current = pageProductDto.getCurrent();
        Integer pageSize = pageProductDto.getPageSize();
        Long id = pageProductDto.getId();
        String name = pageProductDto.getName();
        Integer status = pageProductDto.getStatus();
        //判断分页参数是否合法
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        //添加分页条件
        Page<Product> page = new Page<>(current, pageSize);
        //添加查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(id != null, Product::getId, id);
        wrapper.like(StrUtil.isNotBlank(name), Product::getName, name);
        wrapper.eq(status != null, Product::getStatus, status);
        //查询
        this.page(page, wrapper);
        //返回记录
        return PageBean.of(page.getTotal(), page.getRecords());
    }

    @Override
    public void alterStatus(Long id, Integer status) {
        //判断参数是否合法
        if (id <= 0 || status < 0 || status > 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断ID是否存在
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断当前状态值和数据库状态值是否一样
        if (product.getStatus().equals(status)) {
            return;
        }
        //更新状态
        product.setStatus(status);
        this.updateById(product);
    }
}




