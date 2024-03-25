package com.erimali.cntrymilitary;

import java.util.Arrays;

public class PopDistFloat {
    float[] popDist;

    public PopDistFloat(float... popDist){
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

}
