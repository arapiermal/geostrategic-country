package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum CasusBelli implements CValidatable {
    //Countries that have signed UN -> opinion --
    IMPERIALISM("Imperialism", 100) {

    },
    //Based on claims/previous owners of provinces
    TERRITORY("Territorial dispute", 50) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
        public boolean isValid(World world, int cInd1, int cInd2) {
            Country c1 = world.getCountry(cInd1);
            Country c2 = world.getCountry(cInd2);
            world.getInitialProvinces();
            return false;
        }
    },
    //Linguistic territorial disputes (they have provinces with people that speak the same main language as ours...)
    LINGUISTIC("Linguistic minority", 30) {
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
    //Free countries from subjugation / release countries that have been annexed
    LIBERATE("Liberation", 25) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            //Check for releasable countries in c2
            return false;
        }
    },
    //Change their gov type to the same as ours
    REGIME("Regime change", 30) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.getGovernment().sameType(c2.getGovernment());
        }
    },
    //They have valuable resources (for ex.)
    ECONOMIC_DOMINATION("Economic Domination", 40) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
    },
    //Help rebels take over the country, cannot annex provinces, maybe liberate
    ASSIST_REBELS("Assist our rebels", 40) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
    },
    //They have sponsored rebel groups against us
    SPONSOREDREBELS("Revenge their rebel support",40) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
    },
    //They have continuously carried cyberattacks on us
    //CYBERATTACKED,
    //Only declarable by subjects to the main country
    INDEPENDENCE("Independence", 0) {
        @Override
        public boolean isValid(Country c1, Country c2) {
            if (!c1.isNotSubject())
                return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
            else
                return false;
        }
    };
    //COUNTEROFFENSIVE/SELFDEFENSE
    private final String desc;
    private final short perceivedAggressiveness;

    private EnumSet<WarObjectives> allowedObjectives;

    CasusBelli(String desc, int perceivedAggressiveness) {
        this.desc = desc;
        this.perceivedAggressiveness = (short) perceivedAggressiveness;
    }

    @Override
    public String toString() {
        return this.desc;
    }
    public short getPerceivedAggressiveness(){
        return perceivedAggressiveness;
    }
    @Override
    public boolean isValid(Country c1, Country c2) {
        return true;
    }//based on casus
}