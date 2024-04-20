package com.erimali.cntrygame;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Set;

class EnumShortArray<K extends Enum<K>> {
    Class<K> enumClass;
    short[] arr;

    public EnumShortArray(Class<K> enumClass) {
        this.enumClass = enumClass;
        arr = new short[enumClass.getEnumConstants().length];
        Arrays.fill(arr, (short) 0);
    }

    public void set(K en, short s) {
        arr[en.ordinal()] = s;
    }

    public short get(K en) {
        return arr[en.ordinal()];
    }
    public void inc(K en){
        arr[en.ordinal()]++;
    }

    public static <K extends Enum<K>> void testPut(short[] arr, K en, short s) {
        arr[en.ordinal()] = s;
    }
}

public class TESTING {

    public static void main(String[] args) {
/*
		
		GDate d2 = g.specialDate("now/now/now");
		print(d2);
		Map<String, Integer> vars = new HashMap<>();
		vars.put("i", -1);
		print(EquationSolver.solve(" 1 + 1 * 2", vars));
		String input = "hello, world!";
		String result = Language.uppercaseFirstCharacter(input);
		System.out.println(result);
		*/
        //World w = new World();
        //print(w.randLangName("Albanian:John"));
        //print(BooleanSolver.solve(".prime(137)"));

        //EriSentence s = new EriSentence("abcd,,efg ?!?hij?!?");
        //print(s.getWords());


    }

    public static void print(Object... arg) {
        if (arg.length == 0) {
            System.out.println();
        }
        for (Object o : arg) {
            System.out.println(o);
        }

    }

}

