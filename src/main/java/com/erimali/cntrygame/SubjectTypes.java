package com.erimali.cntrygame;

public enum SubjectTypes{
	PROTECTORATE("Protectorate"){
		
	},SATELLITE("Satellite state"){
		
	},SPACECOLONY("Space colony"){

	};

	SubjectTypes(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	private String name;
}