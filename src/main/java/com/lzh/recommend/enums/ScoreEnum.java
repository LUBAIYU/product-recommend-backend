package com.lzh.recommend.enums;

import lombok.Getter;

/**
 * 分数枚举类
 *
 * @author by
 */
@Getter
public enum ScoreEnum {
    /**
     * 1分
     */
    ONE(1),
    /**
     * 2分
     */
    TWO(2),
    /**
     * 3分
     */
    THREE(3);

    private final Integer score;

    ScoreEnum(Integer score) {
        this.score = score;
    }
}
