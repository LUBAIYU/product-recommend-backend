package com.lzh.recommend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.CommonConsts;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.enums.ScoreEnum;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.mapper.CartMapper;
import com.lzh.recommend.model.dto.PageDto;
import com.lzh.recommend.model.entity.Cart;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.entity.Record;
import com.lzh.recommend.model.vo.CartVo;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.CartService;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.service.RecordService;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.PageBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
        implements CartService {

    @Resource
    private UserService userService;
    @Resource
    private RecordService recordService;
    @Resource
    private ProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long productId, HttpServletRequest request) {
        //判断商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //获取登录的用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();
        //判断用户是否已经添加过该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, productId);
        Cart dbCart = this.getOne(wrapper);
        //如果没有记录则添加购物车记录
        if (dbCart == null) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setNum(1);
            this.save(cart);
            //添加记录
            recordService.addScores(productId, userId);
            return;
        }
        //存在记录则加数量加1
        dbCart.setNum(dbCart.getNum() + 1);
        this.updateById(dbCart);
        //更新记录表，增加积分，作为推荐算法数据源
        recordService.addScores(productId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCart(Long cartId, HttpServletRequest request) {
        //判断购物车是否存在
        Cart cart = this.getById(cartId);
        if (cart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断用户是否有删除的权限
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //点击一次取消是将购买数量减1
        int newNum = cart.getNum() - 1;
        cart.setNum(newNum);
        this.updateById(cart);
        //如果num为0则删除
        if (newNum == 0) {
            this.removeById(cartId);
        }
        //根据商品ID和用户ID去查记录表
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getUserId, userId);
        wrapper.eq(Record::getProductId, cart.getProductId());
        Record record = recordService.getOne(wrapper);
        //对记录表中的分数进行扣分
        if (record == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        int newScore = record.getScore() - ScoreEnum.TWO.getScore();
        //进行扣分
        record.setScore(newScore);
        //更新分数
        recordService.updateById(record);
        //如果分数为0，则删除该记录
        if (newScore == 0) {
            recordService.removeById(record);
        }
    }

    @Override
    public PageBean<CartVo> pageCartInfos(PageDto pageDto, HttpServletRequest request) {
        //判断参数是否合法
        Integer current = pageDto.getCurrent();
        Integer pageSize = pageDto.getPageSize();
        if (current == null || pageSize == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        if (current <= 0 || pageSize < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, CommonConsts.PAGE_PARAMS_ERROR);
        }
        //获取登录的用户ID
        UserVo userVo = userService.getLoginUser(request);
        Long userId = userVo.getId();
        //开启分页
        Page<Cart> page = new Page<>(current, pageSize);
        //根据用户ID去查购物车信息表
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        //分页条件查询
        this.page(page, wrapper);
        //获取数据
        long total = page.getTotal();
        List<Cart> cartList = page.getRecords();
        List<CartVo> cartVoList = new ArrayList<>();
        //如果数据为空直接返回
        if (CollectionUtils.isEmpty(cartList)) {
            return PageBean.of(total, cartVoList);
        }
        //封装数据
        cartVoList = cartList.stream().map(cart -> {
            CartVo cartVo = new CartVo();
            Product product = productService.getById(cart.getProductId());
            cartVo.setCartId(cart.getId());
            BeanUtil.copyProperties(product, cartVo);
            BeanUtil.copyProperties(cart, cartVo);
            return cartVo;
        }).collect(Collectors.toList());
        //返回
        return PageBean.of(total, cartVoList);
    }
}




