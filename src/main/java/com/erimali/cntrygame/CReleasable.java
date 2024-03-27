package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

interface CFormableReleasable{
	boolean hasRequired();
}
//Unify in one class ? boolean to tell the difference
public class CReleasable {
	//Releasable for liberation vs Formable through unification
	//Release Northern Epirus
	private String name;
	private int[] reqProvinces; //Country is released with provinces it doesn't
	public CReleasable(String name, int[] reqProvinces){
		this.name = name;
		this.reqProvinces = reqProvinces;
	}
	private Country releaseCountry(Country c, int cId, SVGProvince[] provinces){
		if(hasRequired(cId, provinces)){

			return new Country(this.name);
		}
		return null;
	}

	//CReleasable
	public boolean hasRequired(int countryId, SVGProvince[] provinces){
		for(int reqProv : reqProvinces){
			if(provinces[reqProv].getOwnerId() != countryId)
				return true;
		}
		return false;
	}
	public List<Integer> getOwnedByCountry(int countryId, SVGProvince[] provinces){
		List<Integer> ownedProvinces = new ArrayList<>();
		for(int reqProv : reqProvinces){
			if(provinces[reqProv].getOwnerId() != countryId)
				ownedProvinces.add(reqProv);
		}
		return ownedProvinces;
	}

	public static List<CReleasable> loadReleasables(String path) {
		List<CReleasable> l = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while((line = br.readLine()) != null) {
				
			}
			return l;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return l;
		} catch (IOException e) {
			e.printStackTrace();
			return l;
		}
	}
	//Events based on formables
	public static GEvent loadGEvent(BufferedReader br) {
		try {
			return null;
		} catch(Exception e) {
			return null;
		}
	}
}
