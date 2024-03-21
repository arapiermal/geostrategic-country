package com.erimali.cntrygame;

enum CasusBelli{
    IMPERIALISM(), //Countries that have signed UN -> opinion --
    TERRITORY, //Territorial disputes (they have provinces with people that speak the same language,etc...)
    LIBERATE, //Free countries from subjugation / release countries that have been annexed
    REGIMECHANGE{
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.getGovernment().getType().equalsIgnoreCase(c2.getGovernment().getType());
        }
    }, //Change their gov type to the same as ours
    ECONOMICDOMINATION, //They have valuable resources (for ex.)
    ASSISTREBELS, //Help rebels take over the country, cannot annex provinces, maybe liberate
    SPONSOREDREBELS, //They have sponsored rebel groups against us
    //CYBERATTACKED, //They have continuously carried cyberattacks on us
    INDEPENDENCE{
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
        }
    }; //Only declarable by subjects to the main country
    //COUNTEROFFENSIVE/SELFDEFENSE
    String desc;
    CasusBelli(){

    }

    public boolean isValid(Country c1, Country c2) {
        return true;
    }//based on casus
}