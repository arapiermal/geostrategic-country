package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum CasusBelli {
    IMPERIALISM("Imperialism"), //Countries that have signed UN -> opinion --
    TERRITORY("Territorial dispute"), //Based on claims/previous owners of provinces
    LINGUISTIC("Linguistic minority") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            short c1Lang = c1.getMainLanguage();
            List<Short> c2Langs = c2.getLanguages();
            for(int i = 1; i < c2Langs.size();i++){
                if(c1Lang == c2Langs.get(i))
                    return true;
            }
            //For country which has embraced multiculturalism -> lang = -1 (?);
            return false;
        }
    }, //Linguistic territorial disputes (they have provinces with people that speak the same main language as ours...)
    LIBERATE("Liberation"){
        @Override
        public boolean isValid(Country c1, Country c2) {
            //Check for releasable countries in c2
            return false;
        }
    }, //Free countries from subjugation / release countries that have been annexed
    REGIME("Regime change") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.getGovernment().getType().equalsIgnoreCase(c2.getGovernment().getType());
        }
    }, //Change their gov type to the same as ours
    ECONOMICDOMINATION("Economic Domination"), //They have valuable resources (for ex.)
    ASSISTREBELS("Assist our rebels"){
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
    }, //Help rebels take over the country, cannot annex provinces, maybe liberate
    SPONSOREDREBELS("Revenge their rebel support"){
        @Override
        public boolean isValid(Country c1, Country c2) {
            return false;
        }
    }, //They have sponsored rebel groups against us
    //CYBERATTACKED, //They have continuously carried cyberattacks on us
    INDEPENDENCE("Independence") {
        @Override
        public boolean isValid(Country c1, Country c2) {
            if (!c1.isNotSubject())
                return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
            else
                return false;
        }
    }; //Only declarable by subjects to the main country
    //COUNTEROFFENSIVE/SELFDEFENSE
    private final String desc;
    private EnumSet<WarObjectives> allowedObjectives;
    private int perceivedAggressiveness;

    CasusBelli(String desc) {
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