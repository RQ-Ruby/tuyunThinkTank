package com.RQ.tuyunthinktank.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;
/**
 * @description 用户角色枚举
 * @author RQ
 * @date 2025/5/31 上午11:27
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserRoleEnum getEnumByValue(String value) {
        // 校验value是否为空,objutil是hutool工具包的一个工具类，用于判断对象是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
