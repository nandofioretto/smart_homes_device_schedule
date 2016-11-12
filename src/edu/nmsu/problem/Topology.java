package edu.nmsu.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by nandofioretto on 11/10/16.
 */
public class Topology {

    private double densityKm2;
    private double gridSideMt;
    private double actuatorRadiusMt;

    ArrayList<ArrayList<String>> clusters = new ArrayList<>();
    Map<String, Integer> mapToCluster = new HashMap<>();

    Map<String, ArrayList<String>> neighbors = new HashMap();
    public Topology(double densityKm2, double gridSideMt, double actuatorRadiusMt) {
        this.densityKm2 = densityKm2;
        this.gridSideMt = gridSideMt;
        this.actuatorRadiusMt = actuatorRadiusMt;

        generate();
    }

    private void generate() {

        // Compute number of clusters and number of agents per cluster
        int numAgents = (int)((densityKm2 * gridSideMt) / 1000);
        int numClusters = (int) Math.ceil(gridSideMt / actuatorRadiusMt);
        int agentsPerCluster = (int) Math.ceil(numAgents / (double) numClusters);

        System.out.println("Generating " + numAgents + " AGENTS " + numClusters + " CLUSTERS " );
        for (int cId = 0; cId < numClusters; cId++) {
            clusters.add(new ArrayList<String>());
        }

        // Save clusters of agents
        for (int aId = 1, cId = 0; aId <= numAgents; aId++)
        {
            String agtName = "home_" + aId;
            (clusters.get(cId)).add(agtName);
            mapToCluster.put(agtName, cId);

            if (aId % agentsPerCluster == 0) {
                cId++;
            }
        }

        for (int aId = 1; aId <= numAgents; aId++) {
            String agtName = "home_" + aId;
            int cId = mapToCluster.get(agtName);
            neighbors.put(agtName, new ArrayList<>(clusters.get(cId)) );
        }

        // Merge Agents in separate clusters:
        for (int cId = 0; cId < numClusters-1; cId++) {
            String this_c = Utilities.genRand(clusters.get(cId ));
            String next_c = Utilities.genRand(clusters.get(cId + 1));

            neighbors.get(this_c).add(next_c);
            neighbors.get(next_c).add(this_c);
        }


    }

    public Set<String> getAgents() {
        return neighbors.keySet();
    }

    ArrayList<String> getNeighbors(String agtName) {
        return neighbors.get(agtName);
    }

}
