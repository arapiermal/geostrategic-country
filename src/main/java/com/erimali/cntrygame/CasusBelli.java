package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum CasusBelli implements CValidatable {
    //Countries that have signed UN -> opinion --
    IMPERIALISM("Imperialism", 100) {

    },
    //Based on claims/previous owners of provinces
    TERRITORY("Territorial dispute", 50) {
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            short cInd1 = (short) args[0];
            Country c2 = world.getCountry(args[1]);
            //change
            world.getInitialProvinces().ownsOrHasSubject(c2, cInd1);
            return false;
        }
    },
    RECOVER("Recover territory", 0){
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            short cInd1 = (short) args[0];
            Country c2 = world.getCountry(args[1]);

            return world.getInitialProvinces().owns(c2, cInd1);
        }

    },
    //Linguistic territorial disputes (they have provinces with people that speak the same main language as ours...)
    LINGUISTIC("Linguistic minority", 30) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

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
        public boolean isValid(World world, int... args) {
            //Check for releasable countries in c2
            if(args.length < 2)
                return false;
            Country c2 = world.getCountry(args[1]);

            return world.getInitialProvinces().ownsOthers(c2);
        }
    },
    //Change their gov type to the same as ours
    REGIME("Regime change", 30) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

            return !c1.getGovernment().sameType(c2.getGovernment());
        }
    },
    //They have valuable resources (for ex.)
    ECONOMIC_DOMINATION("Economic Domination", 40) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

            return false;
        }
    },
    //Help rebels take over the country, cannot annex provinces, maybe liberate
    ASSIST_REBELS("Assist our rebels", 40) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

            return false;
        }
    },
    //They have sponsored rebel groups against us
    SPONSOREDREBELS("Revenge their rebel support", 40) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

            return false;
        }
    },
    //They have continuously carried cyberattacks on us
    //CYBERATTACKED,
    //Only declarable by subjects to the main country
    INDEPENDENCE("Independence", 0) {
        @Override
        public boolean isValid(World world, int... args) {
            if(args.length < 2)
                return false;
            Country c1 = world.getCountry(args[0]);
            Country c2 = world.getCountry(args[1]);

            if (!c1.isNotSubject())
                return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
            else
                return false;
        }
    };
    //COUNTEROFFENSIVE/SELFDEFENSE
    private final String desc;
    private final short perceivedAggressiveness;

    private EnumSet<WarObjective> allowedObjectives;

    CasusBelli(String desc, int perceivedAggressiveness) {
        this.desc = desc;
        this.perceivedAggressiveness = (short) perceivedAggressiveness;
    }

    @Override
    public String toString() {
        return this.desc;
    }

    public short getPerceivedAggressiveness() {
        return perceivedAggressiveness;
    }

    @Override
    public boolean isValid(World world, int... args) {
        return true;
    }//based on casus
}