package com.lzh.recommend.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 图片信息常量类
 *
 * @author lzh
 */
public interface PictureConsts {

    /**
     * 图片url最大长度
     */
    int MAX_URL_LENGTH = 508;

    /**
     * 图片名称最大长度
     */
    int MAX_NAME_LENGTH = 124;

    /**
     * 图片简介最大长度
     */
    int MAX_INTRODUCTION_LENGTH = 508;

    /**
     * 图片文件最大大小
     */
    long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 图片文件进行缩略处理的大小要求
     */
    long THUMBNAIL_FILE_SIZE = 20 * 1024;

    /**
     * 图片文件允许的后缀
     */
    List<String> ALLOW_SUFFIX_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");

    /**
     * 图片文件允许的 Content-Type
     */
    List<String> ALLOW_CONTENT_TYPE_LIST = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
}
