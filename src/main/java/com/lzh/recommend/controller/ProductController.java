package com.lzh.recommend.controller;

import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.annotation.MustAdmin;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.*;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.model.vo.ProductVo;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.Result;
import com.lzh.recommend.utils.ThrowUtils;
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

    @PostMapping("/add")
    @MustAdmin
    public Result<Long> addProduct(@RequestBody ProductAddDto productAddDto) {
        if (productAddDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = productService.addProduct(productAddDto);
        return Result.success(id);
    }

    @GetMapping("/get/{id}")
    @MustAdmin
    public Result<Product> getProductById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Product product = productService.getById(id);
        return Result.success(product);
    }

    @GetMapping("/get/vo/{id}")
    @LoginCheck
    public Result<ProductVo> getProductVoById(@PathVariable Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ProductVo productVo = productService.getProductVoById(id, request);
        return Result.success(productVo);
    }

    @DeleteMapping("/delete/{id}")
    @MustAdmin
    public Result<Boolean> delProduct(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.deleteById(id);
        return Result.success(true);
    }

    @PutMapping("/update")
    @MustAdmin
    public Result<Boolean> updateProduct(@RequestBody ProductUpdateDto productUpdateDto) {
        if (productUpdateDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.updateProduct(productUpdateDto);
        return Result.success(true);
    }

    @PostMapping("/page")
    @MustAdmin
    public Result<PageBean<Product>> listProductsByPage(@RequestBody PageProductDto pageProductDto) {
        if (pageProductDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<Product> pageBean = productService.listProductsByPage(pageProductDto);
        return Result.success(pageBean);
    }

    @PutMapping("/alter/status")
    @MustAdmin
    public Result<Boolean> altStatus(Long id, Integer status) {
        if (id == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.alterStatus(id, status);
        return Result.success(true);
    }

    @PostMapping("/search")
    @LoginCheck
    public Result<PageBean<ProductVo>> searchProducts(@RequestBody SearchProductDto searchProductDto, HttpServletRequest request) {
        if (searchProductDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<ProductVo> pageBean = productService.searchProducts(searchProductDto, request);
        return Result.success(pageBean);
    }


    @PostMapping("/recommend")
    @LoginCheck
    public Result<List<ProductVo>> recommendProducts(Integer count, HttpServletRequest request) {
        if (count == null || count < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<ProductVo> productVos = productService.recommend(count, request);
        return Result.success(productVos);
    }


    @PostMapping("/purchase")
    @LoginCheck
    public Result<Boolean> purchaseProduct(Long productId, HttpServletRequest request) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.purchaseProduct(productId, request);
        return Result.success(true);
    }

    @PostMapping("/add/batch")
    @MustAdmin
    public Result<Boolean> addProductByBatch(@RequestBody ProductFetchDto productFetchDto, HttpServletRequest request) {
        ThrowUtils.throwIf(productFetchDto == null, ErrorCode.PARAMS_ERROR);
        productService.addProductByBatch(productFetchDto, request);
        return Result.success(true);
    }
}
