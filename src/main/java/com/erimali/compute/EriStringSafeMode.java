package com.erimali.compute;

//or Eri String implement this
public abstract class EriStringSafeMode extends EriString{
	
	public EriStringSafeMode(String string) {
		super(string);
	}
	public abstract void encDcdXOR(String key);
	public abstract boolean isEncoded();
	
}
