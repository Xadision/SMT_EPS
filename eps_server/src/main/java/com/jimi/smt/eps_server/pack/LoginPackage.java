package com.jimi.smt.eps_server.pack;

import cc.darhao.jiminal.annotation.Parse;
import cc.darhao.jiminal.annotation.Protocol;
import cc.darhao.jiminal.core.BasePackage;

@Protocol(0x4C)
public class LoginPackage extends BasePackage {

	@Parse({0,6})
	private String centerControllerMAC;

	public String getCenterControllerMAC() {
		return centerControllerMAC;
	}

	public void setCenterControllerMAC(String centerControllerMAC) {
		this.centerControllerMAC = centerControllerMAC;
	}
	
}
