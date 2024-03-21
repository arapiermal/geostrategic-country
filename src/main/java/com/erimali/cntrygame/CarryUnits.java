package com.erimali.cntrygame;

public interface CarryUnits {
	boolean getCanCarry(int i);
	void putUnitsOnBoard(BaseUnit... unit);
	boolean isFull();
}
