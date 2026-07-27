package com.narangnorang.common;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private T result;
	private String message;
	private String source;

	public void setSuccess(T result) {
		this.success = true;
		this.result = result;
		this.message = null;
		this.source = resolveCaller();
	}

	public void setFail(String message) {
		this.success = false;
		this.result = null;
		this.message = message;
		this.source = resolveCaller();
	}

	private String resolveCaller()	 {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		// [0]=getStackTrace, [1]=resolveCaller, [2]=setSuccess/setFail, [3]=실제 호출자
		StackTraceElement caller = stack[3];
		return caller.getClassName() + "." + caller.getMethodName();
	}
}