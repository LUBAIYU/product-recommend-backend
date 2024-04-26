package com.lzh.recommend.controller;

import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.PageDto;
import com.lzh.recommend.model.vo.CartVo;
import com.lzh.recommend.service.CartService;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author by
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private CartService cartService;

    @GetMapping("/add")
    @LoginCheck
    public Result<Void> addCart(Long productId, HttpServletRequest request) {
        if (productId == null || productId <= 0 || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        cartService.addCart(productId, request);
        return Result.success();
    }

    @DeleteMapping("/delete/{cartId}")
    @LoginCheck
    public Result<Void> delCart(@PathVariable Long cartId, HttpServletRequest request) {
        if (cartId == null || cartId <= 0 || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        cartService.deleteCart(cartId, request);
        return Result.success();
    }

    @GetMapping("/page")
    @LoginCheck
    public Result<PageBean<CartVo>> listCartInfosByPage(PageDto pageDto, HttpServletRequest request) {
        if (pageDto == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<CartVo> pageBean = cartService.pageCartInfos(pageDto, request);
        return Result.success(pageBean);
    }
}
