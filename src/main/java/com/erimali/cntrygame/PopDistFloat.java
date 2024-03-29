package com.erimali.cntrygame;

import java.util.Arrays;

public class PopDistFloat {
    float[] popDist;

    public PopDistFloat(float... popDist) {
        this.popDist = genPercent(popDist);
    }

    public static float[] genPercent(float... in) {
        float sum = 0;
        for (float f : in) {
            sum += f;
        }
        if (sum == 100)
            return in;
        float[] out = new float[in.length];
        for (int i = 0; i < out.length; i++) {
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

    void merge(int a1, PopDistFloat o, int a2) {
        float r1 = (float) a1 / (a1 + a2);
        float r2 = (float) a2 / (a1 + a2);
        float[] oldPopDist = popDist;
        if (popDist.length < o.popDist.length) {
            popDist = new float[o.popDist.length];
        }
        int i = 0;
        while (i < oldPopDist.length && i < o.popDist.length) {
            popDist[i] = oldPopDist[i] * r1 + o.popDist[i] * r2;
            i++;
        }
        while (i < oldPopDist.length) {
            popDist[i] = oldPopDist[i] * r1;
            i++;
        }
        while (i < popDist.length) {
            popDist[i] = o.popDist[i] * r2;
            i++;
        }
    }

    public String toString() {
        return Arrays.toString(popDist);
    }

}
