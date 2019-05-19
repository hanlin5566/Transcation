package com.hanson.base.response;

import com.hzcf.base.enums.IResponseCode;

/**
 * Create by hanlin on 2018年10月11日
 **/

public enum AccountResponseCode implements IResponseCode {
	BALANCE_NOT_ENOUGH(6000000, "余额不足。","余额不足，请充值。"),
	ACCOUNT_NOT_FOUND(6000001, "账户未建立。","账户未建立。"),
	ACCOUNT_RECHARGE_OPTIMISTICED_LOCKED(6000002, "充值失败,请稍后重试。","充值过程中触发乐观锁,运算ver%s."),
	;
//	PARAM_ERROR(200001, "变量引擎接收参数异常"),;
	private final int code;
	private final String friendlyMsg;
	private final String detailMsg;

	private AccountResponseCode(int code, String friendlyMsg) {
		this.code = code;
		this.friendlyMsg = friendlyMsg;
		this.detailMsg = friendlyMsg;
	}

	private AccountResponseCode(int code, String friendlyMsg, String detailMsg) {
		this.code = code;
		this.friendlyMsg = friendlyMsg;
		this.detailMsg = detailMsg;
	}

	@Override
	public int code() {
		return code;
	}

	@Override
	public String friendlyMsg() {
		return friendlyMsg;
	}

	@Override
	public String detailMsg() {
		return detailMsg;
	}

	public static AccountResponseCode codeOf(int code) {
        for (AccountResponseCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid ResponseCode code: " + code);
    }
}

