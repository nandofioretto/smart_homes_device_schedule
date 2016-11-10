package edu.nmsu;

import edu.nmsu.communication.DCOPagent;
import edu.nmsu.communication.DCOPinfo;
import edu.nmsu.communication.Spawner;
import edu.nmsu.kernel.DCOPInstance;
import edu.nmsu.kernel.DCOPInstanceFactory;
import edu.nmsu.problem.Generator;
import edu.nmsu.problem.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main {

    public static void main(String[] args) {

//        generateSHDSInstances();
//        return;

        int timeHorizon = Parameters.getHorizon();

        List<Object> algParams = new ArrayList<>();

        int nbIterations = 5;
        long solverTimeoutMs = 10000;
        double wCost = 1;
        double wPower = 1;

        DCOPInstance dcopInstance = DCOPInstanceFactory.importDCOPInstance("resources/instance.json");
        Spawner spawner = new Spawner(dcopInstance);

        algParams.add(nbIterations);
        algParams.add(solverTimeoutMs);
        algParams.add(wCost);
        algParams.add(wPower);

        spawner.spawn(algParams);

//        // Summary Output
        System.out.println(getSummary(spawner.getSpawnedAgents()));
    }


    public static void generateSHDSInstances() {
        Generator gen = new Generator(10, 500, 250);
        gen.topologyGenerator();
    }

    public static String getUsage() {
        return "dcop_jtool FILE.xml [options]\n" +
                "  where options is one of the following:\n" +
                "  --repair (-r) [GDBR, TDBR(default)]. The DLNS repair phase.\n" +
                "  --destroy (-d) [RAND(default), MEETINGS]. The DLNS destroy phase.\n" +
                "  --iterations (-i) (default=500). The number of iterations of DLNS.\n" +
                "  --timeout (-t) (default=no timeout (0)). The simulated time maximal execution time.\n";
    }

    public static String getSummary(Collection<DCOPagent> agents) {
        String res = "time\tIterAgtMsgs\tnAgtMsgs\tNetLoad\tGain\tCost";
        int maxIter = DCOPinfo.leaderAgent.getAgentStatistics().size();
        long maxTime = 0; int nMsgs = 0; int netLoad = 0;

        System.out.println(maxIter);
        for (int iter = 0; iter < maxIter; iter++) {
            int agtMsgs = 0;
            boolean save = true;

            for (DCOPagent agt : agents) {

                if (iter >= agt.getAgentStatistics().size()) continue;

                maxTime = Math.max(maxTime, agt.getAgentStatistics().getMilliTime(iter));
                int msgNow =  agt.getAgentStatistics().getSentMessages(iter);
                int msgPrev = iter == 0 ? 0 : agt.getAgentStatistics().getSentMessages(iter-1);
                nMsgs = Math.max(nMsgs, msgNow);
                agtMsgs = Math.max(agtMsgs, (msgNow - msgPrev));
                netLoad += (msgNow - msgPrev);
            }

            if (save) {
                res += maxTime + "\t";
                res += + agtMsgs + "\t" + nMsgs + "\t" + netLoad + "\n";
            }
        }
        return  res;
    }

}
