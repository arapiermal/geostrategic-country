package com.erimali.cntrymilitary;

import java.io.BufferedReader;
import java.io.FileReader;

public class MilPersonnel {
    //MilType with static info
    //MilUnit for inGame
    //divide personnel in other?!?
    private static final int MAX_TYPES = 3;
    private static int CURR_ID = 0;
    protected final int id;
    protected final byte type;
    //GroundSoldiers, GroundVehicles, (WaterSoldiers/Marines), WaterVehicles,(AirSoldiers/paratroopers or sth), AirVehicles
    //even (0,2,4) personnel, odd (1,3,5) vehicles (?)

    protected String name;
    protected String desc;
    protected int subtype;
    protected int speed;
    //atk and def based on types
    protected int[] atk;
    protected int[] def;
    protected int hp;

    //////////////////////////////////////////////////////////////////////

    protected float morale;
    //protected int maxHealth=hp*maxSize;
    //protected double totalHealth;
    //by this logic there's a direct interdependency between hp and size
    //
    protected int maxSize;
    protected int size;

    protected int xp;
    protected int lvl;

    public MilPersonnel(byte type, String name, String desc) {
        this.id = CURR_ID++;
        this.lvl = 1;
        this.type = type;
        this.name = name;
        this.desc = desc;
    }
    public MilPersonnel(String loc) throws Exception{
        try (BufferedReader br = new BufferedReader(new FileReader(loc))) {
            String line;
            while((line = br.readLine()) != null){
                setValue(line.split(":"));
            }
        }
        this.id = CURR_ID++;
        this.type =(byte) (loc.charAt(loc.length()-1) - '0');
    }

    private void setValue(String[] in){
        switch(in[0].toLowerCase()){
            case "name":
                this.name = in[1];
                break;
            case "desc":
                this.desc = in[1];
                break;
            case "subtype":
                this.subtype = Integer.parseInt(in[1]);
                break;
            case "speed":
                this.speed = Integer.parseInt(in[1]); //speed based on environment?!?
                break;
            case "atk":
                this.atk = new int[in.length - 1];
                for(int i = 1; i < in.length; i++){
                    this.atk[i-1] = Integer.parseInt(in[i]);
                }
                break;
            case "def":
                this.def = new int[in.length - 1];
                for(int i = 1; i < in.length; i++){
                    this.def[i-1] = Integer.parseInt(in[i]);
                }
                break;
            case "hp":
                this.hp = Integer.parseInt(in[1]); //speed based on environment?!?
                break;

        }
    }

    public int recruit(int recruitSize){
        if(this.lvl > 1){
            //!!!!!!!!!!!!!!!!!
            return recruitSize;
            //substract xp until 0 ?!? as counterbalance?!?
            //or take longer to train
            //lvl 2 divide by 2?!?
        }
        this.size += recruitSize;

        //take care of max Size
        if(this.size > this.maxSize){
            int extraManpower = this.size - this.maxSize;
            this.size = this.maxSize;
            return extraManpower;
        }
        return 0;
    }
    public void train(int value){
        //only personnel should be trainable (?)
        this.xp += value;
        //protected int lvlCap;
        int lvlCap = this.lvl * 100;
        if(this.xp > lvlCap){
            this.lvl++;
            this.xp -= lvlCap;
        }
    }
    public void attackUnit(MilPersonnel o, boolean attacking){
        double mATK = this.atk[o.type] * size * Math.sqrt(this.lvl) + this.speed * this.morale + this.xp;
        double oDEF = o.def[this.type] * size * Math.sqrt(o.lvl) + o.speed * o.morale + o.xp;
        double diff1 = mATK-oDEF;


        if(o.stillStanding())
            o.attackUnit(this,false);
    }
    public void attackUnits(MilPersonnel... opponent){
        for(MilPersonnel o : opponent)
            this.attackUnit(o,true);
    }

    public boolean stillStanding(){
        //if size > 0 BUT morale <= 0, army can surrender or retreat (if it can do the later)
        return this.size > 0 && this.morale > 0;
    }

}
