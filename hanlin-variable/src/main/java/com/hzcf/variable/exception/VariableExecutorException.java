package com.hzcf.variable.exception;


/**
 * Create by hanlin on 2017年11月24日
 **/
public class VariableExecutorException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	private String varNmae;
	public String getVarNmae() {
		return varNmae;
	}
	public VariableExecutorException(String varNmae,Exception e) {
		super(e);
		this.varNmae = varNmae;
	}
	
	public VariableExecutorException(String varNmae,String message) {
		super(message);
		this.varNmae = varNmae;
	}
}
