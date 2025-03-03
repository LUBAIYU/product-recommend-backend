package com.lzh.recommend.controller;

import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.annotation.MustAdmin;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.PageProductDto;
import com.lzh.recommend.model.dto.ProductAddDto;
import com.lzh.recommend.model.dto.ProductUpdateDto;
import com.lzh.recommend.model.dto.SearchProductDto;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 商品控制器
 *
 * @author by
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @PostMapping("/image/upload")
    @LoginCheck
    public Result<String> uploadImage(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String imageUrl = productService.uploadImage(multipartFile, request);
        return Result.success(imageUrl);
    }

    @PostMapping("/add/info")
    @MustAdmin
    public Result<Void> addProduct(@RequestBody ProductAddDto productAddDto) {
        if (productAddDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.addProduct(productAddDto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @MustAdmin
    public Result<Void> delProduct(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update/info")
    @MustAdmin
    public Result<Void> updateProduct(@RequestBody ProductUpdateDto productUpdateDto) {
        if (productUpdateDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.updateProduct(productUpdateDto);
        return Result.success();
    }

    @GetMapping("/page")
    @MustAdmin
    public Result<PageBean<Product>> listProductsByPage(PageProductDto pageProductDto) {
        if (pageProductDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<Product> pageBean = productService.listProductsByPage(pageProductDto);
        return Result.success(pageBean);
    }

    @PutMapping("/alter/status")
    @MustAdmin
    public Result<Void> altStatus(Long id, Integer status) {
        if (id == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.alterStatus(id, status);
        return Result.success();
    }

    @GetMapping("/search")
    @LoginCheck
    public Result<PageBean<ProductVo>> searchProducts(SearchProductDto searchProductDto, HttpServletRequest request) {
        if (searchProductDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<ProductVo> pageBean = productService.searchProducts(searchProductDto, request);
        return Result.success(pageBean);
    }


    @GetMapping("/recommend")
    @LoginCheck
    public Result<List<ProductVo>> recommendProducts(Integer count, HttpServletRequest request) {
        if (count == null || count < 0 || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<ProductVo> productVos = productService.recommend(count, request);
        return Result.success(productVos);
    }


    @GetMapping("/purchase")
    @LoginCheck
    public Result<Void> purchaseProducts(Long cartId, HttpServletRequest request) {
        if (cartId == null || cartId <= 0 || request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.purchaseProducts(cartId, request);
        return Result.success();
    }
}
