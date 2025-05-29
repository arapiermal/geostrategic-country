package com.erimali.cntryrandom;

import com.erimali.cntrygame.WorldMap;
import javafx.geometry.Point2D;

import java.util.*;

public class RandWorldMap {
    private Random rand;
    private long seed;
    private double width;
    private double height;
    private int totalProv;
    private int totalCountries;
    private List<GeoPolZone> zones;
    private Set<Integer> countryIds = new HashSet<>();
    private List<RandCountry> countries = new ArrayList<>();
    private Voronoi voronoi;
    private PerlinNoiseElevationGen perlinNoise;

    public RandWorldMap(double width, double height) {
        this.rand = new Random();
        this.seed = rand.nextLong();
        rand.setSeed(seed);
        this.width = width;
        this.height = height;
        //decide number of provinces to be generated based on width & height of default WorldMap
        this.totalProv = calcTotalProv();
        this.totalCountries = calcTotalCountries();
        this.zones = new ArrayList<>(totalProv);
        this.perlinNoise = new PerlinNoiseElevationGen(seed);
    }


    public RandWorldMap(double width, double height, long seed, int totalProv, int totalCountries) {
        this.rand = new Random(seed);
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.totalProv = totalProv;
        if(totalCountries > totalProv)
            this.totalCountries = calcTotalCountries();
        else
            this.totalCountries = calcTotalProv();
        this.zones = new ArrayList<>(totalProv);
        this.perlinNoise = new PerlinNoiseElevationGen(seed);
    }


    public void basicVoronoi() {
        voronoi = new VoronoiMapGen(totalProv, (int) width, (int) height);

    }

    public void jitteredVoronoi() {
        int[] rowsCols = JitteredGridVoronoi.calcRowsColsFromTotalProv(totalProv, width, height);
        jitteredVoronoi(rowsCols[0], rowsCols[1]);
    }

    public void jitteredVoronoi(int rows, int cols) {
        this.totalProv = rows * cols;
        voronoi = new JitteredGridVoronoi(rows, cols, width, height, 0.4);
    }


    public void relaxVoronoi(int n){
        if(voronoi != null && n > 1){
            voronoi.relax(n);
        }
    }

    public void reset(){
        zones.clear();
        // clear other stuff as well
    }

    public Voronoi getVoronoi(){
        return voronoi;
    }

    public void generateZones(){
        if(voronoi == null){
            return;
        }
        reset();
        int octaves = 4;
        double persistence = 0.5;
        double scale = 0.005;

        List<Point2D> sites = voronoi.getSites();
        List<List<Point2D>> cells = voronoi.getVoronoiCells();
        int n = sites.size();
        for(int i = 0; i < n; i++){
            Point2D site = sites.get(i);
            List<Point2D> boundary = cells.get(i);
            double x = site.getX();
            double y = site.getY();

            double elevation = perlinNoise.islandNoise(x,y,width,height,octaves,persistence,scale);
            GeoPolZone zone;
            if(elevation < GeoPolZone.getSeaLevel()){
                zone = new RandWaterBody(site, boundary);
                zone.setElevation(elevation);
            } else{
                zone = new RandProvince(site, boundary);
                zone.setElevation(elevation);
            }
            zones.add(zone);

        }
    }

    public void generateCountries(){


    }

    protected int calcTotalProv() {
        double ratioWidth = width / WorldMap.getDefMapWidth();
        double ratioHeight = height / WorldMap.getDefMapHeight();
        int p = WorldMap.getDefProvCount();
        return (int) (p * ratioWidth * ratioHeight * rand.nextDouble(0.9, 1.1));
    }

    protected int calcTotalCountries() {
        return Math.max(1, totalProv / 10);
    }

    public long getSeed() {
        return seed;
    }

    public int getTotalProv() {
        return totalProv;
    }

    public double getMapWidth() {
        return width;
    }

    public double getMapHeight() {
        return height;
    }

    public List<GeoPolZone> getZones() {
        return zones;
    }
}
