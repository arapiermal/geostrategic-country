package com.erimali.cntrygame;

import java.util.Arrays;

public class PopDistDouble {
    double[] popDist;

    public PopDistDouble(double... popDist) {
        this.popDist = genPercent(popDist);
    }

    public static double[] genPercent(double... in) {
        double sum = 0;
        for (double f : in) {
            sum += f;
        }
        if (sum == 100)
            return in;
        double[] out = new double[in.length];
        for (int i = 0; i < out.length; i++) {
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

    void merge(long a1, PopDistDouble o, long a2) {
        double r1 = (double) a1 / (a1 + a2);
        double r2 = (double) a2 / (a1 + a2);
        double[] oldPopDist = popDist;
        if (popDist.length < o.popDist.length) {
            popDist = new double[o.popDist.length];
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

    void increase(int indInc, double val, int indDec) {
        if (indInc < 0 || indInc > popDist.length || indDec < 0 || indDec > popDist.length)
            return;
        popDist[indInc] += val;
        popDist[indDec] -= val;
    }

    public String toString() {
        return Arrays.toString(popDist);
    }


    public static void main(String... args) {
        PopDistDouble p1 = new PopDistDouble(10, 20, 40, 15, 15);
        PopDistDouble p2 = new PopDistDouble(20, 20, 40, 20);
        p1.merge(15, p2, 10);
        TESTING.print(p1);
    }
}
