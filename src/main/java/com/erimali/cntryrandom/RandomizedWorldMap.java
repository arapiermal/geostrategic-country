package com.erimali.cntryrandom;

import com.erimali.cntrygame.WorldMap;

import java.util.Random;
class Point{
    double x;
    double y;
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    // calc distance and set minimal distance (?)

    @Override
    public String toString(){
        return x + " : " + y;
    }
}
// use SVGProvince directly (?)
class RandomizedProv {
    double centerX;
    double centerY;
    // store shape data
    int ownerId; // can be set later
    //group provinces based on their nearness and create RandomizedCountry

    // random population and calculated area...
}
class RandomizedCountry {
    //can be more general, to work with main game regardless
    //
    public RandomizedCountry(Random rnd, RandomizedProv... provinces) {

    }
}

//This can use the Voronoi Map Gen (or others) so make it generalized (?) (? abstract)
public class RandomizedWorldMap {
    private final Random rnd;
    private final long seed;
    private final double width;
    private final double height;
    private final int totalProv;
    private final RandomizedProv[] provinces;
    public RandomizedWorldMap(double width, double height){
        this.rnd = new Random();
        this.seed = rnd.nextLong();
        rnd.setSeed(seed);
        this.width = width;
        this.height = height;
        //decide number of provinces to be generated based on width & height of default WorldMap
        this.totalProv = calcTotalProv();
        this.provinces = new RandomizedProv[totalProv];
        generateProvinces();
    }


    public RandomizedWorldMap(double width, double height, long seed, int totalProv) {
        this.rnd = new Random(seed);
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.totalProv = totalProv;
        this.provinces = new RandomizedProv[totalProv];
        generateProvinces();
    }

    protected int calcTotalProv(){
        //rename getDefMapHeight()
        double ratioWidth = width / WorldMap.getDefMapWidth();
        double ratioHeight = height / WorldMap.getDefMapHeight();
        int p = WorldMap.getDefProvCount();
        return (int) (p * ratioWidth * ratioHeight * rnd.nextDouble(0.9,1.1));
    }

    private void generateProvinces() {
        // create more points and discard some of them in certain regions to simulate waters
        // totalProv + (ratio water/earth)(?) * totalProv
        // 70% land -> totalProv, 30% water -> y
        // calculate biggest watermass
        Point[] points = new Point[totalProv];
        for(int i = 0; i < totalProv; i++){
            points[i] = randomPoint();
            System.out.println(i+ " -> " + points[i]);
        }
        //centerX and centerY recalculated later...
    }

    private Point randomPoint() {
        return new Point(rnd.nextDouble() * width, rnd.nextDouble() * height);
    }

    public long getSeed(){
        return seed;
    }
    public int getTotalProv(){
        return totalProv;
    }
}
