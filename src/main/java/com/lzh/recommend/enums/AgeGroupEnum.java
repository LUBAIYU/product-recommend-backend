package com.lzh.recommend.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 年龄分组枚举
 *
 * @author lzh
 */
@Getter
public enum AgeGroupEnum {

    /**
     * 青少年
     */
    TEENAGER(1, 18),
    /**
     * 年轻人
     */
    YOUNG_ADULT(18, 35),
    /**
     * 中年人
     */
    MIDDLE_AGED(35, 60),
    /**
     * 老年人
     */
    OLD_AGED(60, 99);

    private final int ageStart;

    private final int ageEnd;

    AgeGroupEnum(int ageStart, int ageEnd) {
        this.ageStart = ageStart;
        this.ageEnd = ageEnd;
    }

    /**
     * 根据年龄获取枚举值
     *
     * @param age 年龄
     * @return 年龄组
     */
    public static AgeGroupEnum getEnumByAge(Integer age) {
        if (ObjectUtil.isEmpty(age)) {
            return null;
        }
        for (AgeGroupEnum ageGroupEnum : AgeGroupEnum.values()) {
            if (ageGroupEnum.getAgeStart() <= age && age < ageGroupEnum.getAgeEnd()) {
                return ageGroupEnum;
            }
        }
        return null;
    }

    /**
     * 判断两个年龄是否属于同一分组
     *
     * @param age1 年龄1
     * @param age2 年龄2
     * @return 是否相同
     */
    public static boolean isSameGroup(Integer age1, Integer age2) {
        return Objects.equals(getEnumByAge(age1), getEnumByAge(age2));
    }
}
