package com.erimali.cntryrandom;

public class TestExecutionTimes {
    public static void main(String[] args) {
        //new RandWorldMap(200, 200).basicVoronoi(); // warm-up
        UsefulColors.loadUsefulColors();
        RandWorldMap map1 = new RandWorldMap(1000, 500);
        RandWorldMap map2 = new RandWorldMap(2000, 1000);
        RandWorldMap map3 = new RandWorldMap(4000, 2000);
        long totalTime1 = testBasicVoronoi(map1);
        long totalTime2 = testBasicVoronoi(map2);
        long totalTime3 = testBasicVoronoi(map3);
        System.out.println("Testing the generation of other attributes for the political map");
        totalTime1 += testWholeProcess(map1);
        totalTime2 += testWholeProcess(map2);
        totalTime3 += testWholeProcess(map3);
        System.out.println("Testing the generation of geographical map (Perlin Noise)");
        PerlinMapFX.genPerlinMapImage(new RandWorldMap(200, 200)); // warm-up because of JavaFX
        totalTime1 += testPerlin(map1);
        totalTime2 += testPerlin(map2);
        totalTime3 += testPerlin(map3);
        System.out.println("Total Times");
        System.out.println(totalTime1);
        System.out.println(totalTime2);
        System.out.println(totalTime3);

    }

    public static long testBasicVoronoi(RandWorldMap map) {
        double width = map.getMapWidth();
        double height = map.getMapHeight();
        long start = System.currentTimeMillis();
        map.basicVoronoi();
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("Basic Voronoi: " + width + "x" + height + " took " + diff + " ms");
        System.out.println("Province count: " + map.getTotalProv());
        return diff;
    }

    public static long testWholeProcess(RandWorldMap randWorldMap) {
        double width = randWorldMap.getMapWidth();
        double height = randWorldMap.getMapHeight();
        long start = System.currentTimeMillis();
        randWorldMap.generateAll();
        long end = System.currentTimeMillis();

        long diff = end - start;
        System.out.println(width + "x" + height + " took " + diff + " ms");
        System.out.println("Countries count: " + randWorldMap.getTotalCountries());
        return diff;
    }

    public static long testPerlin(RandWorldMap randWorldMap){
        double width = randWorldMap.getMapWidth();
        double height = randWorldMap.getMapHeight();
        long start = System.currentTimeMillis();
        PerlinMapFX.genPerlinMapImage(randWorldMap);
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println(width + "x" + height + " took " + diff + " ms");
        return diff;
    }
}
