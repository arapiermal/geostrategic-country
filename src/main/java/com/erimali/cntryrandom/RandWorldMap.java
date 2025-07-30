package com.erimali.cntryrandom;

import com.erimali.cntrygame.CountryArray;
import com.erimali.cntrygame.WorldMap;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.util.*;

public class RandWorldMap {
    private Random rand;
    private long seed;
    private double width;
    private double height;
    private int totalProv;
    private int totalCountries;
    private List<GeoPolZone> zones;
    private List<RandProvince> provinces;
    private List<RandWaterBody> waterBodies;
    private Set<Integer> countryIds = new HashSet<>();
    private List<RandCountry> countries = new ArrayList<>();
    private Voronoi voronoi;
    private PerlinNoiseElevationGen perlinNoise;
    private int perlinOctaves = 4;
    private double perlinPersistence = 0.5;
    private double perlinScale = 0.005;

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
        this.provinces = new ArrayList<>();
        this.waterBodies = new ArrayList<>();
        this.perlinNoise = new PerlinNoiseElevationGen(seed, width, height, perlinOctaves, perlinPersistence, perlinScale);
    }


    public RandWorldMap(double width, double height, long seed, int totalProv, int totalCountries) {
        this.rand = new Random(seed);
        this.seed = seed;
        this.width = width;
        this.height = height;
        this.totalProv = totalProv;
        if (totalCountries > totalProv)
            this.totalCountries = calcTotalCountries();
        else
            this.totalCountries = calcTotalProv();
        this.zones = new ArrayList<>(totalProv);
        this.provinces = new ArrayList<>();
        this.waterBodies = new ArrayList<>();
        this.perlinNoise = new PerlinNoiseElevationGen(seed, width, height, perlinOctaves, perlinPersistence, perlinScale);
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


    public void relaxVoronoi(int n) {
        if (voronoi != null && n > 1) {
            voronoi.relax(n);
        }
    }

    public void reset() {
        zones.clear();
        provinces.clear();
        waterBodies.clear();
        GeoPolZone.resetCountingIDS();
        countryIds.clear();
        countries.clear();
    }

    public Voronoi getVoronoi() {
        return voronoi;
    }

    public void generateAll(){
        generateZones();
        generateCountries();
        generateColors();
        //generateProvinceNames();
    }

    public void generateProvinceNames() {
        for(RandCountry c : countries)
            c.generateNamesForProvinces();
    }

    private void generateColors() {
        UsefulColors.colorCountries(countries);
    }

    public void generateZones() {
        if (voronoi == null) {
            return;
        }
        reset();

        List<Point2D> sites = voronoi.getSites();
        List<List<Point2D>> cells = voronoi.getVoronoiCells();
        int n = sites.size();
        for (int i = 0; i < n; i++) {
            Point2D site = sites.get(i);
            List<Point2D> boundary = cells.get(i);
            double x = site.getX();
            double y = site.getY();

            double elevation = perlinNoise.islandNoise(x, y);
            if (elevation < GeoPolZone.getSeaLevel()) {
                RandWaterBody water = new RandWaterBody(site, boundary);
                water.setElevation(elevation);
                waterBodies.add(water);
                zones.add(water);
            } else {
                RandProvince prov = new RandProvince(site, boundary);
                prov.setElevation(elevation);
                provinces.add(prov);
                zones.add(prov);
            }
        }

        GeoPolZone.findAndAssignNeighbors(zones, false);
    }

    public void generateCountries() {
        Set<RandProvince> visited = new HashSet<>();

        for (RandProvince startProv : provinces) {
            if (visited.contains(startProv)) continue;
            int countryId = CountryArray.genISO2ID(countryIds);
            RandCountry newCountry = new RandCountry(countryId);
            // later method for coloroing
            //newCountry.setColor(Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));

            List<RandProvince> countryProvs = newCountry.getProvinces();
            Queue<RandProvince> queue = new LinkedList<>();
            queue.add(startProv);
            visited.add(startProv);
            int desiredCountrySize = totalProv / totalCountries;
            while (!queue.isEmpty() && countryProvs.size() < desiredCountrySize) {
                RandProvince prov = queue.poll();
                countryProvs.add(prov);
                prov.setCountry(newCountry);

                for (RandProvince neighbor : prov.getProvinceNeighbours()) {
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }

            countries.add(newCountry);
        }
        // Second pass: assign leftover provinces to nearest country
        for (RandProvince leftover : provinces) {
            if (leftover.getCountry() != null) continue;

            for (RandProvince neighbor : leftover.getProvinceNeighbours()) {
                if (neighbor.getCountry() != null) {
                    RandCountry country = neighbor.getCountry();
                    leftover.setCountry(country);
                    country.getProvinces().add(leftover);
                    break;
                }
            }

            // Optional: isolated provinces still left -> Create tiny country
            if (leftover.getCountry() == null) {
                int countryId = CountryArray.genISO2ID(countryIds);
                RandCountry tinyCountry = new RandCountry(countryId);
                tinyCountry.getProvinces().add(leftover);
                leftover.setCountry(tinyCountry);
                countries.add(tinyCountry);
            }
        }
        calculateCountryNeighbors();
    }

    private void calculateCountryNeighbors() {
        for (RandCountry country : countries) {
            Set<RandCountry> neighborCountries = country.getNeighbours();
            for (RandProvince prov : country.getProvinces()) {
                for (RandProvince neigh : prov.getProvinceNeighbours()) {
                    if (neigh.getCountry() != null && neigh.getCountry() != country) {
                        neighborCountries.add(neigh.getCountry());
                    }
                }
            }
        }
    }

    protected int calcTotalProv() {
        double ratioWidth = width / WorldMap.getDefMapWidth();
        double ratioHeight = height / WorldMap.getDefMapHeight();
        int p = WorldMap.getDefProvCount() * 5;
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

    public PerlinNoiseElevationGen getPerlinNoise() {
        return perlinNoise;
    }

    public int getTotalCountries() {
        return countries != null ? countries.size() : 0;
    }
}
