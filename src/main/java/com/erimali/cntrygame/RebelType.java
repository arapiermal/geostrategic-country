package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum RebelType implements CValidatable {
    //Rebels that speak the same main language as us
    LINGUISTIC("Linguistic minority") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            short c1Lang = c1.getMainLanguage();
            List<Short> c2Langs = c2.getLanguages();
            for (int i = 1; i < c2Langs.size(); i++) {
                if (c1Lang == c2Langs.get(i))
                    return true;
            }
            //For country which has embraced multiculturalism -> lang = -1 (?);
            return false;
        }
    },
    //Rebels against their government
    REGIME("Regime change") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.getGovernment().getType().equalsIgnoreCase(c2.getGovernment().getType());
        }
    },
    //Rebels that desire independence from subjugation/[annexation]
    INDEPENDENCE("Independence") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            if (!c1.isNotSubject())
                return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
            else
                return false;
        }
    };
    private final String desc;
    private EnumSet<WarObjectives> allowedObjectives;
    private short perceivedAggressiveness;

    RebelType(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }


    public boolean isValid(Country c1, Country c2) {
        return true;
    }//based on casus
}