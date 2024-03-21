package com.erimali.cntrygame;

import java.util.ArrayList;
import java.util.List;

interface BaseUnit {
	// for civilians (cars?), space ship astronauts
}

interface BaseVehicles {

}

interface BaseNavy {

}

interface BaseAirforce {

}

interface BaseSpaceforce {

}

abstract class MilWeapon {
	MilAmmo ammunition;

}

//for upgradeable purposes? better ammo
abstract class MilAmmo {

}

abstract class MilUnit implements BaseUnit, Cloneable {
	static final int UNITTYPENUMBER = 4;

	String name;
	int qnt; // quantity
	int maxQnt; // when recruiting/building, also when damaged/recovering
	int quality; // grade //special forces for MilSoldiers?
	int training; 
	int level;// calc level with sqrt training?
	int atk;
	int def;
	int subtype;

	static TravelAmbients mainMovement;

	// polymorphic methods
	public abstract void attack(MilUnit... op);

	public abstract void train(int amount);

	public boolean join(MilUnit unit) {
		if (this.getClass() == unit.getClass() && this.subtype == unit.subtype) {
			this.training = (this.training * this.qnt + unit.training * unit.qnt) / (2 * (this.qnt + unit.qnt));

			this.qnt += unit.qnt;
			this.maxQnt += unit.maxQnt;

			return true;
		} else {
			return false;
		}
	}

	// better way?
	public void setLevel() {
		this.level = (int) Math.sqrt(training / 10);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}

//Certain types do better against other types
class MilSoldiers extends MilUnit {
	static {
		mainMovement = TravelAmbients.TERRAIN;
	}

	MilSoldiers(String name, int subtype, int maxQnt) {
		this.name = name;
		this.subtype = subtype;
		this.maxQnt = maxQnt;

	}

	/*
	 * MilSoldiers(MilSoldiers copy, int qnt){}
	 */
	// Weapon types
	MilWeapon weapon;

	// if Anti-tank, Anti-air weapons -> more damage
	@Override
	public void attack(MilUnit... op) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return qnt + "x soldiers";
	}

	@Override
	public void train(int amount) {
		this.training += amount;
	}

	// public void build for others? maybe percentage?
	// is amount correct????
	public int recruit(int amount) {
		int result = -1;// not yet finished
		qnt += amount;
		if (qnt >= maxQnt) {
			result = maxQnt - qnt;
			qnt = maxQnt;
		}
		return result;
	}

}

class MilMarine extends MilSoldiers {

	MilMarine(String name, int subtype, int qnt) {
		super(name, subtype, qnt);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return qnt + "x marines";
	}
}

//Artilliery?
class MilVehicles extends MilUnit implements BaseVehicles {
	static {
		mainMovement = TravelAmbients.TERRAIN;
	}
	// how many inside vehicles?

	static final String NAMEOFTYPES[] = { "Tank", "Vehicle" }; // HMMM
	static final int ATKOFTYPES[] = { 100, 50 };
	static final int DEFOFTYPES[] = { 100, 50 };

	public MilVehicles(int subtype, int qnt) {
		this.subtype = subtype;
		name = NAMEOFTYPES[subtype];
		atk = ATKOFTYPES[subtype];
	}

	@Override
	public void attack(MilUnit... op) {
		// TODO Auto-generated method stub
	}

	@Override
	public void train(int amount) {
		// TODO Auto-generated method stub

	}

}

class TransportVehicles extends MilVehicles implements CarryUnits {
	List<MilUnit> carrying;
	boolean[] canCarry;
	int[] maxCarry;
	public TransportVehicles(int subtype, int qnt) {
		super(subtype, qnt);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getCanCarry(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putUnitsOnBoard(BaseUnit... unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}

}

class MilNavy extends MilUnit implements BaseNavy {
	static {
		mainMovement = TravelAmbients.WATER;
	}
	MilMarine marines;

	@Override
	public void attack(MilUnit... op) {
		// TODO Auto-generated method stub

	}

	@Override
	public void train(int amount) {
		// TODO Auto-generated method stub

	}

}

class TransportNavy extends MilNavy implements CarryUnits {
	List<MilUnit> carrying;
	boolean[] canCarry;
	int[] maxCarry;
	@Override
	public boolean getCanCarry(int i) {
		return canCarry[i];
	}

	@Override
	public void putUnitsOnBoard(BaseUnit... unit) {

	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}
}

class MilAirforce extends MilUnit implements BaseAirforce {
	static {
		mainMovement = TravelAmbients.AIR;
	}

	@Override
	public void attack(MilUnit... op) {
		// TODO Auto-generated method stub

	}

	@Override
	public void train(int amount) {
		// TODO Auto-generated method stub

	}

}

class TransportAirforce extends MilAirforce implements CarryUnits {
	List<MilUnit> carrying;
	boolean[] canCarry;

	@Override
	public boolean getCanCarry(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putUnitsOnBoard(BaseUnit... unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}
}

class MilSpaceforce extends MilUnit implements BaseSpaceforce {

	@Override
	public void attack(MilUnit... op) {
		// TODO Auto-generated method stub

	}

	@Override
	public void train(int amount) {
		// TODO Auto-generated method stub

	}
}

class TransportSpaceforce extends MilSpaceforce implements CarryUnits {
	boolean[] canCarry;

	@Override
	public boolean getCanCarry(int i) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putUnitsOnBoard(BaseUnit... unit) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}

}

public class MilDivision {
	private static final int DEFAULT_TRAINING = 10;
	// HMMMMMM, this is already a division
	public List<MilUnit> units;
	public List<MilSoldiers> recruitingSoldiers;
	public List<MilUnit> trainingUnits;

	public MilDivision() {
		units = new ArrayList<>();
		recruitingSoldiers = new ArrayList<>();
		trainingUnits = new ArrayList<>();
	}

	public void trainSoldiers(String name, int subtype, int qnt) {
		MilSoldiers soldiers = new MilSoldiers(name, subtype, qnt);
		recruitingSoldiers.add(soldiers);
	}

	// IRRELEVANT????
	// SINCE IS DIVISION
	// inefficient thinking (since beginning)?
	// 9999 soldiers -> 9x1000 soldiers + 999 soldiers
	public void distributeSplitUnits(MilUnit unit, int qntEach) {
		while (unit.qnt > qntEach) {
			splitUnit(unit, qntEach);
		}
	}

	public void splitUnit(MilUnit unit, int splitQnt) {
		if (splitQnt <= 0 || splitQnt >= unit.qnt) {
			throw new IllegalArgumentException("Invalid split quantity.");
		}
		try {
			MilUnit newUnit = (MilUnit) unit.clone();
			unit.qnt -= splitQnt;
			unit.maxQnt -= splitQnt;
			newUnit.qnt = splitQnt;
			newUnit.maxQnt = splitQnt;
			units.add(newUnit);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * Constructor<? extends MilUnit> constructor; try { constructor =
		 * unit.getClass().getConstructor(MilUnit.class, int.class); MilUnit newUnit =
		 * constructor.newInstance(unit, splitQnt); unit.qnt -= splitQnt;
		 * units.add(newUnit);
		 * 
		 * } catch(Exception e) {}
		 */
	}

	public void joinSimilarUnits() {
		if (units.size() < 2) {
			return;
		}
		// THEY HAVE TO BE THE SAME TYPE
		for (int i = 0; i < units.size(); i++) {
			for (int j = i + 1; j < units.size(); j++) {
				if (units.get(i).join(units.get(j))) {
					units.remove(j);
				}
			}
		}

	}

	public void move() {
		// make already recruited soldiers moveable
	}

	// on interrupt
	public void interruptRecruitingSoldiers() {
		// maxQnt = qnt
		// maxQnt-qnt = > new maxQnt
	}

	public int recruitTick(int amount) {
		// better way, amount -
		int equallyDistributed = amount / recruitingSoldiers.size();
		int manpowerExtra = amount % recruitingSoldiers.size();
		int i = 0;

		for (MilSoldiers ms : recruitingSoldiers) {
			int extra;
			if (i++ == 0 || manpowerExtra > 0) {
				extra = ms.recruit(equallyDistributed + manpowerExtra);
			} else {
				extra = ms.recruit(equallyDistributed);
			}
			if (extra == -1) {

			} else if (extra >= 0) {
				manpowerExtra += extra;
				units.add(ms);
				recruitingSoldiers.remove(ms);
			}

		}

		return manpowerExtra;
	}

	public void trainTick(int amount) {
		// divide amount / trainingUnits.length
		for (MilUnit mu : trainingUnits) {
			mu.train(amount);
		}
	}

	public void startTraining(int... index) {
		for (int i : index) {
			trainingUnits.add(units.remove(i));
		}
	}

	public void stopTraining(int... index) {
		for (int i : index) {
			// MilUnit mu = trainingUnits.get(i);
			units.add(trainingUnits.remove(i));
		}
	}

	public void attack(MilUnit... opponent) {
		// distribute?
		// 2 : 5 -> 1st-> 2, 2nd -> 2, 1st->1
		int skip = units.size() / opponent.length;
		int p = 0;
		for (int i = 0; i < opponent.length; i += skip) {
			units.get(p).attack(opponent[i]); // from 1 to 2?
		}
	}

}