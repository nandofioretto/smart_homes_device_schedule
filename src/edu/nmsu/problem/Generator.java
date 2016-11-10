package edu.nmsu.problem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Generates SHDS problems
 */
public class Generator {

    private double densityKm2;
    private double gridSideMt;
    private double actuatorRadiusMt;
    private final int timeHorizon = Parameters.getHorizon();
    private final double[] priceSchema = Parameters.getPriceSchema();

    Random rand = new Random();

    public Generator(double densityKm2, double gridSideMt, double actuatorRadiusMt) {
        this.densityKm2 = densityKm2;
        this.gridSideMt = gridSideMt;
        this.actuatorRadiusMt = actuatorRadiusMt;
    }

    double[] generateBackgroundLoad() {
        double[] bg = new double[timeHorizon];
        for (int i = 0; i < timeHorizon; i++) {
            bg[i] = rand.nextDouble();
        }
        return bg;
    }

    // todo: make version where all agents are constrained with all other agents
    public void topologyGenerator() {

        // number of agents
        int numAgents = (int)((densityKm2 * gridSideMt) / 1000);
        int numClusters = (int) Math.ceil(gridSideMt / actuatorRadiusMt);
        int agentsPerCluster = (int) Math.ceil(numAgents / (double) numClusters);

        ArrayList<ArrayList<String>> clusters = new ArrayList<>();
        Map<String, Integer> mapToCluster = new HashMap<>();

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

        // All agents within a cluster share a constraint

        try {
            JSONObject jExperiment = new JSONObject();
            jExperiment.put("horizon", 12);
            jExperiment.put("priceSchema", priceSchema);
            //jExperiment.put("agents", );

            JSONObject jAgents = new JSONObject();
            for (String agtName : mapToCluster.keySet()) {
                JSONObject jAgent = new JSONObject();

                // Create array of neighbors
                JSONArray jNeighbors = new JSONArray();
                int cId = mapToCluster.get(agtName);
                for (String neigName : clusters.get(cId)) {
                    if (neigName.compareTo(agtName) != 0)
                        jNeighbors.put(neigName);
                }

                jAgent.put("neighbors", jNeighbors);
                jAgent.put("backgroundLoad", generateBackgroundLoad());
                // todo:
                jAgent.put("rules", "TODO");

                jAgents.put(agtName, jAgent);
            }
            jExperiment.put("agents", jAgents);

            System.out.println(jExperiment.toString(2));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
