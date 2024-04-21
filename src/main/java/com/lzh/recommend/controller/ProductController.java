package com.lzh.recommend.controller;

import cn.hutool.core.util.StrUtil;
import com.lzh.recommend.annotation.LoginCheck;
import com.lzh.recommend.enums.ErrorCode;
import com.lzh.recommend.exception.BusinessException;
import com.lzh.recommend.model.dto.PageProductDto;
import com.lzh.recommend.model.dto.ProductAddDto;
import com.lzh.recommend.model.dto.ProductUpdateDto;
import com.lzh.recommend.model.entity.Product;
import com.lzh.recommend.service.ProductService;
import com.lzh.recommend.service.UserService;
import com.lzh.recommend.utils.PageBean;
import com.lzh.recommend.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

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
    @Resource
    private UserService userService;

    @Value("${product.recommend.path.product-prefix}")
    private String prefix;

    @PostMapping("/upload/image")
    @LoginCheck
    public Result<String> uploadImage(MultipartFile multipartFile) {
        if (multipartFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String imageUrl = userService.uploadImage(multipartFile, prefix);
        return Result.success(imageUrl);
    }

    @GetMapping("/get/image/{fileName}")
    @LoginCheck
    public void getImage(@PathVariable String fileName, HttpServletResponse response) {
        if (StrUtil.isBlank(fileName) || response == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.getImage(fileName, response);
    }


    @PostMapping("/add/info")
    public Result<Void> addProduct(@RequestBody ProductAddDto productAddDto) {
        if (productAddDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.addProduct(productAddDto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delProduct(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update/info")
    public Result<Void> updateProduct(@RequestBody ProductUpdateDto productUpdateDto) {
        if (productUpdateDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.updateProduct(productUpdateDto);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageBean<Product>> listProductsByPage(PageProductDto pageProductDto) {
        if (pageProductDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        PageBean<Product> pageBean = productService.listProductsByPage(pageProductDto);
        return Result.success(pageBean);
    }

    @PutMapping("/alter/status")
    public Result<Void> altStatus(Long id, Integer status) {
        if (id == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        productService.alterStatus(id, status);
        return Result.success();
    }
}
