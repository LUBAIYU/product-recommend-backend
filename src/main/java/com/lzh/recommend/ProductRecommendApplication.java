package com.lzh.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author by
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class ProductRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductRecommendApplication.class, args);
    }

}
