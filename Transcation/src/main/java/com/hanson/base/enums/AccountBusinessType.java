package com.hanson.base.enums;

import com.hzcf.base.enums.EnumType;

/**
 * Create by hanlin on 2017年11月6日
 * 账户异动类型
 **/
public enum AccountBusinessType implements EnumType {
	UNKNOWN(0, "未知"),
	MALE(1, "充值"),
	FEMALE(2, "扣款"),
    ;


    private final int code;
    private final String text;

    private AccountBusinessType(int code, String text) {
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

    public static AccountBusinessType codeOf(int code) {
        for (AccountBusinessType value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid DataStatus code: " + code);
    }

    public static void main(String[] args) {
        System.out.println(AccountBusinessType.codeOf(1).text);
    }
}
