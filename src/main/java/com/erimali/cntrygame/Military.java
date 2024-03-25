package com.erimali.cntrygame;

import java.util.*;

import javafx.scene.shape.SVGPath;

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
	//named divisions (?)
	private long manpower;
	private List<MilDivision> divisions;
	private Set<Short> atWarWith;

	private GDate lastDeclaredWar;


	public Military() {
		divisions = new ArrayList<>();
		atWarWith = new HashSet<>();
	}
	public void addDivision(MilDivision d) {
		divisions.add(d);
	}
	//FULL SCALE ATTACK???
	//SEMI SCALEATACK??
	public void attackOpMil(Military op) {
		
	}
	//hmmm
	public void attackOpMil(List<MilDivision> m,List<MilDivision> op) {
		
	}
	
}
