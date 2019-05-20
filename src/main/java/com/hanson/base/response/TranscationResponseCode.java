package com.hanson.base.response;

import com.hzcf.base.enums.IResponseCode;

/**
 * Create by hanlin on 2018年10月11日
 **/

public enum TranscationResponseCode implements IResponseCode {
	PARAM_ERROR(200001, "参数解析异常,请检查参数。","参数解析异常,请检查参数。"),
	AES_DECRYPT_ERROR(200002, "AES解密异常。","AES解密异常,密文"),
	AES_ENCRYPT_ERROR(200003, "AES加密异常。","AES加密发生异常"),
	AES_GENERATEKEY_ERROR(200004, "AES初始化秘钥异常。","AES加密发生异常"),
	DATA_IS_NOT_JSON_TYPE(200005, "数据不是JSON格式。","转换时为JSON格式发生异常"),
	ACCOUNT_EMPTY(200006, "接口帐号为空。","接口帐号为空,入参"),
	SIGNATUER_EMPTY(200007, "接口秘钥为空。","接口秘钥为空,入参"),
	AUTHORIZED_FAILED(200008, "授权失败，请检查帐号或者秘钥。","授权失败，请检查帐号或者秘钥."),
	PARAM_CAN_NOT_FOUND(200009, "未找到参数。","未找到参数."),
	PARAM_IS_NOT_JSON_TYPE(2000010, "参数结构不符合规范。","参数结构不符合规范."),
	REQUIRED_PARAMETER_MISSING(2000011, "参数缺失，缺少三要素参数。","参数缺失，缺少三要素参数."),
	REQUIRED_NAME_MISSING(2000012, "参数缺失，姓名为空。","参数缺失，姓名为空."),
	NAME_LENGTH_OUT_BOUND(2000013, "姓名超过限制长度。","姓名超过长度超过20字符"),
	REQUIRED_MOBILE_MISSING(2000014, "参数缺失，电话号码为空。","参数缺失，电话号码为空"),
	MOBILE_LENGTH_OUT_BOUND(2000015, "电话号码超过限制长度。","电话号码超过长度超过20字符"),
	DECISION_PARAM_NOT_FOUND(2000016, "决策失败，参数缺失。","决策失败，参数缺失。"),
	DECISION_TOPO_NOT_FOUND(2000017, "决策失败，未找到决策拓扑。","规则集[%s],决策失败，未找到决策拓扑。"),
	CALL_FETCH_DATA_ERROR(2000018, "调用取数接口失败。","调用取数接口因[%s]失败。"),
	CONVERT_DATA_ERROR(2000019, "转换数据体失败。","转换数据体因[%s]失败。"),
	ASYNC_SERVICE_LOG_ERROR(2000030, "异步服务日志错误。","异步服务日志引发%s错误。"),
	SYSTEM_USER_NOT_FOUND(2000050, "系统用户未找到。","系统用户未找到引发%s错误。"),
	;
//	PARAM_ERROR(200001, "变量引擎接收参数异常"),;
	private final int code;
	private final String friendlyMsg;
	private final String detailMsg;

	private TranscationResponseCode(int code, String friendlyMsg) {
		this.code = code;
		this.friendlyMsg = friendlyMsg;
		this.detailMsg = friendlyMsg;
	}

	private TranscationResponseCode(int code, String friendlyMsg, String detailMsg) {
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

	public static TranscationResponseCode codeOf(int code) {
        for (TranscationResponseCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid ResponseCode code: " + code);
    }
}

