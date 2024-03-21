package com.erimali.cntrygame;

public enum Syntax {
	END("kaq");
	Syntax(String name){
		this.name = name;
	}
	private String name;
	public String getName() {
		return name;
	}
}
