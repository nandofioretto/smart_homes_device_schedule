package edu.nmsu;

import edu.nmsu.Home.Home;
import edu.nmsu.Home.LocalSolver.CPSolver;
import edu.nmsu.Home.LocalSolver.RuleSchedulerSolver;
import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.problem.Parameters;

import java.io.IOException;

public class Main {


    public static void main(String[] args) {

        Home home = new Home("homeA");
        try {
            home.readSchedulingRules("resources/RuleFile_0.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(home.toString());

        int timeHorizon = Parameters.getHorizon();
        double[] bgLoads = new double[timeHorizon]; // all zeros
        double[] neighborLoads = new double[timeHorizon]; // all zeros
        for (int j = 0; j < bgLoads.length; j++) bgLoads[j] = 0;
        for (int j = 0; j < neighborLoads.length; j++) neighborLoads[j] = 0;

        CPSolver scheduler = new RuleSchedulerSolver(home.getName(), home.getSchedulingRules(), bgLoads);
        RulesSchedule rs = scheduler.getSchedule(neighborLoads);
            //scheduler.getBaselineSchedule(neighborLoads);
        System.out.println(rs.toString());

//        DCOPInstance dcopInstance = DCOPInstanceFactory.importDCOPInstance(file);
//        Spawner spawner = new Spawner(dcopInstance);
//        algParams.add(destroyPhase);
//        algParams.add(repairPhase);
//        algParams.add(nbIterations);
//        algParams.add(timeoutMs);
//        spawner.spawn(algParams);
//
//        // Summary Output
//        System.out.println(getSummary(spawner.getSpawnedAgents(), nbIterations));

    }
}
