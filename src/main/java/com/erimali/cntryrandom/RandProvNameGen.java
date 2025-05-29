package com.erimali.cntryrandom;

import java.util.Random;

public class RandProvNameGen {
    private static final Random rand = new Random();
    private static final int totalCultures = RandCultures.values().length;

    // Vowel infixes
    private static final String[] infixes = {"a", "e", "i", "o", "u", "ae", "io", ""};

    public static String generateName() {
        return generateName(RandCultures.values()[rand.nextInt(totalCultures)]);
    }

    public static String generateName(RandCultures culture){
        String prefix = culture.getRandomPrefix(rand);
        String infix = infixes[rand.nextInt(infixes.length)];
        String suffix = culture.getRandomSuffix(rand);
        return prefix + infix + suffix;
    }

}
