package com.erimali.cntryrandom;

import java.util.Random;

public enum RandCultures {
    LATIN(
            new String[]{"Alba", "Roma", "Flor", "Ven", "Luc", "Aquila", "Novi", "San", "Monte"},
            new String[]{"inia", "polis", "orum", "aria", "ante", "ella", "a"}
    ),
    SLAVIC(
            new String[]{"Novo", "Zlat", "Vlad", "Rus", "Bor", "Kras", "Gora", "Miro", "Vel"},
            new String[]{"grad", "ov", "sk", "ich", "nik", "ova", "dor"}
    ),
    NORSE(
            new String[]{"Skal", "Thor", "Eld", "Ulf", "Drak", "Ragn", "Bjorn", "Fenr", "Asg"},
            new String[]{"heim", "fjord", "gard", "skell", "borg", "havn"}
    ),
    GERMANIC(
            new String[]{"Grim", "Falk", "Wulf", "Hild", "Brun", "Otto", "Diet", "Luth", "Wil"},
            new String[]{"berg", "hold", "thal", "wald", "stein", "dorf"}
    ),
    ARABIC(
            new String[]{"Al", "Dar", "Shar", "Qal", "Sah", "Bak", "Abd", "Zay", "Nas"},
            new String[]{"an", "ar", "ir", "abad", "dun", "stan", "rah"}
    ),
    PERSIAN(
            new String[]{"Zar", "Mir", "Jam", "Far", "Shah", "Khor", "Rost", "Tah", "Nim"},
            new String[]{"mir", "shahr", "an", "ruz", "istan", "ban", "far"}
    ),
    CELTIC(
            new String[]{"Inis", "Dun", "Bryn", "Loch", "Aber", "Caer", "Kil", "Tor", "Glen"},
            new String[]{"more", "wick", "dale", "ness", "firth", "bridge"}
    ),
    FANTASY(
            new String[]{"Elar", "Loth", "Syl", "Nim", "Aer", "Faer", "Myth", "Il", "Thal"},
            new String[]{"thien", "dor", "loth", "riel", "ven", "mir", "eth"}
    ),
    ALBANIAN(
            new String[]{"Shko", "Dib", "Ber", "Tep", "Kru", "Val", "Sar", "Gji", "Kor"},
            new String[]{"ës", "anë", "ajë", "ovë", "tar", "nik", "dur"}
    );

    private final String[] prefixes;
    private final String[] suffixes;

    RandCultures(String[] prefixes, String[] suffixes) {
        this.prefixes = prefixes;
        this.suffixes = suffixes;
    }

    public static RandCultures getRandomCulture() {
        return values()[(int) (Math.random() * values().length)];
    }

    public String[] getPrefixes() {
        return prefixes;
    }

    public String[] getSuffixes() {
        return suffixes;
    }

    public String getRandomPrefix(Random rand) {
        return prefixes[rand.nextInt(prefixes.length)];
    }

    public String getRandomSuffix(Random rand) {
        return suffixes[rand.nextInt(suffixes.length)];
    }
}
