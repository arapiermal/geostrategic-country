package com.erimali.cntrygame;

import java.util.Map;

import javafx.scene.shape.SVGPath;

import java.util.HashMap;
import java.util.List;
class MilImg{
	SVGPath svg;
	public MilImg(String path) {
		svg = new SVGPath();
		svg.setContent(path);
	}
	// soldier cap
	// navy symbol
	// airplane symbol
}
public class Military {
	//named divisions
	
	private Map<String, MilDivision> divisions;
	
	public Military() {
		divisions = new HashMap<>();
	}
	public void addDivision(String name, MilDivision d) {
		divisions.put(name, d);
	}
	//FULL SCALE ATTACK???
	//SEMI SCALEATACK??
	//hmmmmm
	public void attackOpMil(Military op) {
		
	}
	//hmmm
	public void attackOpMil(List<MilDivision> m,List<MilDivision> op) {
		
	}
	
}
