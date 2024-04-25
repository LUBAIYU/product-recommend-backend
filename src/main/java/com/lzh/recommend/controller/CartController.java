package com.lzh.recommend.controller;

import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.service.CartService;
import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/add")
    @LoginCheck
    public Result<Void> addCart(Long productId, HttpServletRequest request) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        cartService.addCart(productId, request);
        return Result.success();
    }
}
