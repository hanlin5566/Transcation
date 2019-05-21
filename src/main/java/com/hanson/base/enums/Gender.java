package com.hanson.base.enums;

import com.hanson.base.enums.EnumType;

/**
 * Create by hanlin on 2017年11月6日
 **/
public enum Gender implements EnumType {
	UNKNOWN(0, "未知"),
	MALE(1, "男"),
	FEMALE(2, "女"),
    ;


    private final int code;
    private final String text;

    private Gender(int code, String text) {
        this.code = code;
        this.text = text;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String text() {
        return text;
    }

    public static Gender codeOf(int code) {
        for (Gender value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid DataStatus code: " + code);
    }

    public static void main(String[] args) {
        System.out.println(Gender.codeOf(1).text);
    }
}
