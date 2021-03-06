package com.jimi.smt.eps_server.entity;

public class ResultJson {

	private int code;

	private String msg;

	private Object data;

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "ResultJson [code=" + code + ", msg=" + msg + ", data=" + data + "]";
	}

}
