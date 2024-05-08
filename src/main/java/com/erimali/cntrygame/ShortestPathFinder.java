package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ShortestPathFinder {
    private Map<Integer, int[]> provinceData;

    public ShortestPathFinder(Map<Integer, int[]> provinceData) {
        this.provinceData = provinceData;
    }

    public List<Integer> findShortestPath(int source, int destination) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1])); // Priority queue (distance, vertex)
        Map<Integer, Integer> distance = new HashMap<>(); // Distance from source to each vertex
        Map<Integer, Integer> parent = new HashMap<>(); // Parent vertex of each vertex in the shortest path tree

        // Initialization
        for (int vertex : provinceData.keySet()) {
            distance.put(vertex, Integer.MAX_VALUE);
            parent.put(vertex, null);
        }
        distance.put(source, 0);
        pq.offer(new int[]{source, 0});

        // Dijkstra's algorithm
        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int vertex = current[0];
            int dist = current[1];
            if (dist > distance.get(vertex)) {
                continue;
            }

            if (vertex == destination) {
                break;
            }
            int[] provNeighbours = provinceData.get(vertex);
            if (provNeighbours != null)
                for (int neighbor : provNeighbours) {
                    int newDist = dist + 1; // Assuming unit distance between neighboring provinces
                    int oldDist = distance.getOrDefault(neighbor, Integer.MAX_VALUE);
                    if (newDist < oldDist) {
                        distance.put(neighbor, newDist);
                        parent.put(neighbor, vertex);
                        pq.offer(new int[]{neighbor, newDist});
                    }
                }

        }

        // Reconstruct the shortest path if destination is reachable
        List<Integer> shortestPath = new ArrayList<>();
        if (parent.get(destination) != null) {
            int current = destination;
            while (current != source) {
                shortestPath.add(current);
                current = parent.get(current);
            }
            shortestPath.add(source);
            Collections.reverse(shortestPath);
        }
        return shortestPath;
    }

    public static Map<Integer, int[]> generateNeighbourMap(BufferedReader br) throws IOException {
        Map<Integer, int[]> map = new HashMap<>();
        String line;
        while ((line = br.readLine()) != null) {
            int colInd = line.indexOf(':');
            if (colInd > 0) {
                try {
                    int provId = Integer.parseInt(line.substring(0, colInd));
                    String[] str = line.substring(colInd + 1).split("\\s*,\\s*");
                    int[] neighbours = GUtils.parseIntArr(str);
                    map.put(provId, neighbours);
                } catch (Exception e) {

                }
            }
        }
        return map;
    }

    public static Map<Integer, int[]> generateNeighbourMap() {
        try (BufferedReader br = new BufferedReader(new FileReader(GLogic.RESOURCESPATH + "countries/dijkstra.data"))) {
            return generateNeighbourMap(br);
        } catch (IOException ioe) {
            return null;
        }

    }

    public static void main(String[] args) {
        Map<Integer, int[]> provinceData = generateNeighbourMap();

        ShortestPathFinder finder = new ShortestPathFinder(provinceData);
        int sourceProvince = 3444;
        int destinationProvince = 3446;
        List<Integer> shortestPath = finder.findShortestPath(sourceProvince, destinationProvince);
        if (shortestPath.isEmpty()) {
            System.out.println("No path exists between the source and destination provinces.");
        } else {
            System.out.println("Shortest path: " + shortestPath);
            System.out.println("Number of provinces in the shortest path: " + shortestPath.size());
        }
    }
}
