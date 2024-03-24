package com.erimali.cntrygame;

//instead abstract ... CPolicy, and positive/negative/removable
public enum GovPolicy {
    NEUTRALITY("", 0),
    FORCED_NEUTRALITY("",-1),
    DEMILITARIZED("", -8),
    BANNED_MILITARY("", -16),
    INCENTIVIZE_BIRTHS("", 4),
    INCENTIVIZE_HEALTHINESS("", 4),
    //TAX_CUT("", 8),

    //CYBERSECURITY_MEASURES("",16),
    //VACCINATION_MANDATES("",4),
    ;

    //Positive
    //Negative/imposed policies
    //value for AI to know whether it is a positive or negative policy
    private String desc;
    private int val;

    GovPolicy(String desc, int val) {
        this.desc = desc;
        this.val = val;
    }

    public boolean isRemovable(){
        return val < 0;
    }
}
