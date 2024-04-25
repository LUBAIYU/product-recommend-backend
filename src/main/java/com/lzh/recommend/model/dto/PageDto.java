package com.lzh.recommend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页参数
 *
 * @author by
 */
@Data
public class PageDto implements Serializable {
    /**
     * 当前页码
     */
    private Integer current;
    /**
     * 每页记录数
     */
    private Integer pageSize;
}
