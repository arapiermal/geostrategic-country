package com.erimali.miltest;

import com.erimali.cntrygame.TESTING;

import java.util.Arrays;

public class PopulationManager {
    float[] popDist;

    public PopulationManager(float... popDist){
        this.popDist = genPercent(popDist);
    }

    public static float[] genPercent(float... in){
        float sum = 0;
        for(float f : in){
            sum+=f;
        }
        if(sum == 100)
            return in;
        float[] out = new float[in.length];
        for(int i = 0; i < out.length; i++){
            out[i] = (in[i] / sum) * 100;
        }
        return out;
    }

    void increase(int ind, float val) {
        if (ind < 0 || ind > popDist.length)
            return;
        popDist[ind] += val;
        float decrease = val / (popDist.length - 1);
        for (int i = 0; i < popDist.length; i++) {
            if (i != ind) {
                popDist[i] -= decrease;
                if (popDist[i] < 0) {
                    popDist[i] = 0;
                }
            }

        }
    }
    void increase(int indInc, float val, int indDec) {
        if (indInc < 0 || indInc > popDist.length || indDec < 0 || indDec > popDist.length)
            return;
        popDist[indInc] += val;
        popDist[indDec] -= val;
    }
    public String toString(){
        return Arrays.toString(popDist);
    }
    public static void main(String... args){
        PopulationManager p = new PopulationManager(10,20,70);
        TESTING.print(p);
        p.increase(0,10, 1);
        TESTING.print(p);
        float[] o = genPercent(30,40,10,21);

        TESTING.print(Arrays.toString(o));
    }
}
