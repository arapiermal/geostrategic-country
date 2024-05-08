package com.erimali.cntrygame;
// Java Program to Implement Dijkstra's Algorithm
// Using Priority Queue

// Importing utility classes

import java.util.*;

// Main class DPQ
public class GFG {
    private double dist[];
    private Set<Integer> settled;
    private PriorityQueue<Node> pq;
    // Number of vertices
    private int V;
    List<List<Node>> adj;

    public GFG(int V) {

        // This keyword refers to current object itself
        this.V = V;
        dist = new double[V];
        settled = new HashSet<Integer>();
        pq = new PriorityQueue<Node>(V, new Node());
    }

    // Dijkstra's Algorithm
    public void dijkstra(List<List<Node>> adj, int src, int dst) {
        this.adj = adj;

        for (int i = 0; i < V; i++)
            dist[i] = Integer.MAX_VALUE;

        // Add source node to the priority queue
        pq.add(new Node(src, 0));

        // Distance to the source is 0
        dist[src] = 0;

        while (settled.size() != V) {

            // Terminating condition check when the priority queue is empty, return
            if (pq.isEmpty())
                return;

            // Removing the minimum distance node from the priority queue
            int u = pq.remove().node;

            // Adding the node whose distance is
            // finalized
            if (settled.contains(u))

                // Continue keyword skips execution for following check
                continue;

            // We don't have to call e_Neighbors(u) if u is already present in the settled set.
            settled.add(u);

            e_Neighbours(u);
        }
    }
    // To process all the neighbours of the passed node
    private void e_Neighbours(int u) {

        double edgeDistance = -1;
        double newDistance = -1;

        // All the neighbors of v
        for (int i = 0; i < adj.get(u).size(); i++) {
            Node v = adj.get(u).get(i);

            // If current node hasn't already been processed
            if (!settled.contains(v.node)) {
                edgeDistance = v.cost;
                newDistance = dist[u] + edgeDistance;

                // If new distance is cheaper in cost
                if (newDistance < dist[v.node])
                    dist[v.node] = newDistance;

                // Add the current node to the queue
                pq.add(new Node(v.node, dist[v.node]));
            }
        }
    }

    public static void main(String arg[]) {

        int V = 5;
        int source = 0;

        // Adjacency list representation of the connected edges by declaring List class object
        // Declaring object of type List<Node>
        //List<Node[]> adj;
        List<List<Node>> adj = new ArrayList<List<Node>>();

        for (int i = 0; i < V; i++) {
            List<Node> item = new ArrayList<Node>();
            adj.add(item);
        }

        // Inputs for the GFG(dpq) graph
        adj.get(0).add(new Node(1, 9));
        adj.get(0).add(new Node(2, 6));
        adj.get(0).add(new Node(3, 5));
        adj.get(0).add(new Node(4, 3));

        adj.get(2).add(new Node(1, 2));
        adj.get(2).add(new Node(3, 4));

        // Calculating the single source shortest path
        GFG dpq = new GFG(V);
        dpq.dijkstra(adj, source, 0);

        System.out.println("The shorted path from node :");

        for (int i = 0; i < dpq.dist.length; i++)
            System.out.println(source + " to " + i + " is "
                    + dpq.dist[i]);
    }
}

class Node implements Comparator<Node> {
    public int node;
    public double cost;

    public Node() {
    }

    public Node(int node, double cost) {
        this.node = node;
        this.cost = cost;
    }

    public Node(SVGProvince s0, SVGProvince s1) {
        this.node = s1.getProvId();
        this.cost = s0.getDistance(s1);
    }

    @Override
    public int compare(Node node1, Node node2) {
        return Double.compare(node1.cost, node2.cost);

    }
}
