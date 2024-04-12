package com.lzh.recommend.exception;

import com.lzh.recommend.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义异常处理类
 *
 * @author by
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public static <T> Result<T> error(BusinessException be) {
        return Result.error(be.getCode(), be.getMessage());
    }
}
