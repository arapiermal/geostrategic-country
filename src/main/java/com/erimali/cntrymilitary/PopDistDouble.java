package com.erimali.cntrymilitary;

import java.util.Arrays;

public class PopDistDouble {
    double[] popDist;

    public PopDistDouble(double... popDist){
        this.popDist = genPercent(popDist);
    }

    public static double[] genPercent(double... in){
        double sum = 0;
        for(double f : in){
            sum+=f;
        }
        if(sum == 100)
            return in;
        double[] out = new double[in.length];
        for(int i = 0; i < out.length; i++){
            out[i] = (in[i] / sum) * 100;
        }
        return out;
    }

    void increase(int ind, double val) {
        if (ind < 0 || ind > popDist.length)
            return;
        popDist[ind] += val;
        double decrease = val / (popDist.length - 1);
        for (int i = 0; i < popDist.length; i++) {
            if (i != ind) {
                popDist[i] -= decrease;
                if (popDist[i] < 0) {
                    popDist[i] = 0;
                }
            }

        }
    }
    void increase(int indInc, double val, int indDec) {
        if (indInc < 0 || indInc > popDist.length || indDec < 0 || indDec > popDist.length)
            return;
        popDist[indInc] += val;
        popDist[indDec] -= val;
    }
    public String toString(){
        return Arrays.toString(popDist);
    }
    
}
