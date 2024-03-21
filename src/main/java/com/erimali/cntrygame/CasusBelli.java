package com.erimali.cntrygame;

enum CasusBelli{
    IMPERIALISM("Imperialism"), //Countries that have signed UN -> opinion --
    TERRITORY("Territorial dispute"), //Territorial disputes (they have provinces with people that speak the same language,etc...)
    LIBERATE("Liberation"), //Free countries from subjugation / release countries that have been annexed
    REGIME("Regime change"){
        @Override
        public boolean isValid(Country c1, Country c2) {
            return !c1.getGovernment().getType().equalsIgnoreCase(c2.getGovernment().getType());
        }
    }, //Change their gov type to the same as ours
    ECONOMICDOMINATION("Economic Domination"), //They have valuable resources (for ex.)
    ASSISTREBELS("Assist our rebels"), //Help rebels take over the country, cannot annex provinces, maybe liberate
    SPONSOREDREBELS("Revenge their sponsoring of rebels in our country"), //They have sponsored rebel groups against us
    //CYBERATTACKED, //They have continuously carried cyberattacks on us
    INDEPENDENCE("Independence"){
        @Override
        public boolean isValid(Country c1, Country c2) {
            if(!c1.isNotSubject())
                return !c1.equals(c2) && c1.getSubjectOf().getMain().equals(c2);
            else
                return false;
        }
    }; //Only declarable by subjects to the main country
    //COUNTEROFFENSIVE/SELFDEFENSE
    private String desc;
    CasusBelli(String desc){
        this.desc = desc;
    }
    public String getDesc(){
        return this.desc;
    }
    public boolean isValid(Country c1, Country c2) {
        return true;
    }//based on casus
}