package com.lzh.recommend.exception;

import com.lzh.recommend.enums.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常
 *
 * @author by
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
