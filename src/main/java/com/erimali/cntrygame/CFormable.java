package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CFormable {
	public static class BaseAdmDivs{
		public int[][] provinces = new int[CountryArray.maxISO2Countries][];
		//This can be unnecessary if you add to List<Integer> conqueredCountries after annexation...
		public boolean contains(Country c, int... countries){
			//What if using BigAdmDiv in the first place to avoid these problems?
			for(AdmDiv a : c.getAdmDivs()){

			}

			return false;
		}
		//or each AdmDiv has previousOwner (or the formables inject themselves (?))
	}
	//Releasable for liberation vs Form-able through unification
	//Form Greater Albania !!
	private String name;
	private int[] reqProvinces; //required provinces
	public CFormable(String name, int[] reqProvinces){
		this.name = name;
		this.reqProvinces = reqProvinces;
	}
	private void formCountry(Country c){
		if(canForm(c)){

			c.setName(name);

		}
	}

	private boolean canForm(Country c){
		//TreeSet of provinces from elsewhere (?)

		return false;
	}


	public static List<CFormable> loadFormables(String path) {
		List<CFormable> l = new ArrayList<>();
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
