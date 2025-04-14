package com.lzh.recommend;

import com.lzh.recommend.upload.UrlPictureUpload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ProductRecommendApplicationTests {

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Value("${product.recommend.path.product-image-prefix}")
    private String prefix;

    @Test
    void contextLoads() {
    }

    @Test
    void testUrlUpload() {
        String picUrl = urlPictureUpload.uploadPicture("https://www.codefather.cn/logo.png", prefix + "/aaa");
        System.out.println(picUrl);
    }
}
