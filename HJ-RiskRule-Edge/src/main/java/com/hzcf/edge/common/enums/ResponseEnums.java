package com.hzcf.edge.common.enums;

/**
 * Created by liqinwen on 2018/5/31.
 */
public enum ResponseEnums {

    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "Bad Request"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    PARAM_ERROR(403, "参数有误"),
    SYSTEM_ERROR(500,"系统异常"),
    AUTH_ERROR(999,"认证失败"),
    NO_LOGIN(100,"未登录")
    ;
    private final int code;
    private final String msg;

    ResponseEnums(int code, String msg) {
        this.code=code;
        this.msg = msg;
    }

    public String toString() {
        return String.valueOf(this.code);
    }

    public static int codeOf(int ordinal) {
        for (ResponseEnums value : values()) {
            if (value.ordinal()==ordinal) {
                return value.code;
            }
        }
        return 200;
    }
}

