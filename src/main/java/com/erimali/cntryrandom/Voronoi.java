package com.erimali.cntryrandom;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface Voronoi {
    List<List<Point2D>> getVoronoiCells();
    List<Point2D> getSites();
    void setSites(List<Point2D> sites);
    void generateVoronoiCells();

    // Lloyd's Relaxation
    default void relax(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.generateVoronoiCells();
            List<Point2D> newSites = new ArrayList<>();

            for (List<Point2D> cell : this.getVoronoiCells()) {
                if (cell == null || cell.isEmpty()) continue;
                double sumX = 0, sumY = 0;
                for (Point2D p : cell) {
                    sumX += p.getX();
                    sumY += p.getY();
                }
                newSites.add(new Point2D(sumX / cell.size(), sumY / cell.size()));
            }

            this.setSites(newSites);
        }
    }

    default Point2D getRandomSite(Random rand){
        return getSites().get(rand.nextInt(getSites().size()));
    }

}
