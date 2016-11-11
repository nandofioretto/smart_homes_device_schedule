package edu.nmsu;

import edu.nmsu.communication.DCOPagent;
import edu.nmsu.communication.DCOPinfo;
import edu.nmsu.communication.Spawner;
import edu.nmsu.kernel.DCOPInstance;
import edu.nmsu.kernel.DCOPInstanceFactory;
import edu.nmsu.problem.Generator;
import edu.nmsu.problem.Parameters;
import edu.nmsu.problem.RuleGenerator;
import edu.nmsu.problem.Topology;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main {

    public static void execute() {
        int timeHorizon = Parameters.getHorizon();

        List<Object> algParams = new ArrayList<>();

        int nbIterations = 5;
        long solverTimeoutMs = 5000;
        double wCost = 1;
        double wPower = 1;

        DCOPInstance dcopInstance = DCOPInstanceFactory.importDCOPInstance("resources/instance_1.json");
        Spawner spawner = new Spawner(dcopInstance);

        algParams.add(nbIterations);
        algParams.add(solverTimeoutMs);
        algParams.add(wCost);
        algParams.add(wPower);

        spawner.spawn(algParams);

//        // Summary Output
        System.out.println(getSummary(spawner.getSpawnedAgents()));
    }

    public static void main(String[] args) {
        generateSHDSInstances();
        execute();
        return;

    }


    public static void generateSHDSInstances() {
        Topology topo = new Topology(10, 500, 250);
        RuleGenerator ruleGen = new RuleGenerator();
        Generator gen = new Generator(topo, ruleGen, 8);

        JSONObject exp = gen.generate();

        try {
            FileWriter file = new FileWriter("resources/instance_1.json");
            file.write(exp.toString(2));
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        String res = "time\tIterAgtMsgs\tnAgtMsgs\tNetLoad\tGain\tCost\n";
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
