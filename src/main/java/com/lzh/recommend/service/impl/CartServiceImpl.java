package com.lzh.recommend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzh.recommend.constant.UserConsts;
import com.lzh.recommend.mapper.CartMapper;
import com.lzh.recommend.model.entity.Cart;
import com.lzh.recommend.model.vo.UserVo;
import com.lzh.recommend.service.CartService;
import com.lzh.recommend.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author by
 */
@Service
@Slf4j
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
        implements CartService {

    @Resource
    private RecordService recordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCart(Long productId, HttpServletRequest request) {
        //获取登录的用户ID
        Object object = request.getSession().getAttribute(UserConsts.USER_LOGIN_STATE);
        UserVo userVo = (UserVo) object;
        Long userId = userVo.getId();
        //添加购物车记录
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setNum(1);
        this.save(cart);
        //更新记录表，增加积分，作为推荐算法数据源
        recordService.addScores(productId, userId);
    }
}




