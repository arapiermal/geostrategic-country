package com.erimali.cntryrandom;

import java.util.Random;

public class PerlinNoiseElevationGen {
    private final int[] p;
    private final Random rand;

    public PerlinNoiseElevationGen(long seed) {
        rand = new Random(seed);
        p = new int[512];
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) permutation[i] = i;
        for (int i = 0; i < 256; i++) {
            int j = rand.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        for (int i = 0; i < 512; i++) p[i] = permutation[i & 255];
    }

    // Noise with octaves
    public double octaveNoise(double x, double y, int octaves, double persistence, double scale) {
        double total = 0;
        double maxValue = 0;
        double frequency = 1;
        double amplitude = 1;

        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency * scale, y * frequency * scale) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }

        return total / maxValue;
    }

    // Island-shaped noise (masking based on distance from center)
    public double islandNoise(double x, double y, double width, double height, int octaves, double persistence, double scale) {
        double nx = x / width - 0.5;
        double ny = y / height - 0.5;
        double distance = Math.sqrt(nx * nx + ny * ny) / 0.7071; // Max distance is sqrt(0.5^2 + 0.5^2)
        double base = octaveNoise(x, y, octaves, persistence, scale);
        double mask = 1.0 - Math.pow(distance, 2.5); // sharper island dropoff
        return base * mask;
    }

    // Core Perlin noise function
    public double noise(double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int A = p[X] + Y, AA = p[A], AB = p[A + 1];
        int B = p[X + 1] + Y, BA = p[B], BB = p[B + 1];

        return lerp(v, lerp(u, grad(p[AA], x, y), grad(p[BA], x - 1, y)),
                lerp(u, grad(p[AB], x, y - 1), grad(p[BB], x - 1, y - 1)));
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 3;
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }


}
