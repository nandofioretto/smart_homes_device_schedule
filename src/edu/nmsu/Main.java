package edu.nmsu;

import edu.nmsu.communication.DCOPagent;
import edu.nmsu.communication.DCOPinfo;
import edu.nmsu.communication.Spawner;
import edu.nmsu.kernel.DCOPInstance;
import edu.nmsu.kernel.DCOPInstanceFactory;
import edu.nmsu.problem.*;
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
        double wCost = 5;
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
        //for (int i=0; i < 50; i++) {
            //generateSHDSInstances();
            execute();
        //}
        return;

    }


    public static void generateSHDSInstances() {
        Topology topo = new Topology(5, 1000, 1000);
        RuleGenerator ruleGen = new RuleGenerator();
        Generator gen = new Generator(topo, ruleGen, 4);

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
        String res = "iterNo\t"
                + "simTime\t"
                + "AvgCPTime\t"
                + "AvgMsgsSentIter\t"
                + "NetLoad\t"
                + "SumScheduleCost\t"
                + "SumPriceCost\t"
                + "AvgPriceCost\t"
                + "energyCost\t"
                + "avgEnergyCost\t"
                + "vgGain\n";

        int maxIter = DCOPinfo.leaderAgent.getAgentStatistics().size();
        long simTime = 0; int netLoad = 0;

        for (int iter = 0; iter < maxIter; iter++) {
            int iterMsgsSent = 0;
            double sumScheduleCost = 0;
            double sumPriceCost    = 0;
            double sumPowerCost    = 0;
            double avgGain         = 0;
            double avgCPtime       = 0;
            for (DCOPagent agt : agents) {
                if (iter >= agt.getAgentStatistics().size()) continue;

                // time
                simTime = Math.max(simTime, agt.getAgentStatistics().getMilliTime(iter));
                avgCPtime += agt.getAgentStatistics().getSchedulingTimeMsIter(iter);

                // msgs
                int msgNow =  agt.getAgentStatistics().getSentMessages(iter);
                int msgPrev = iter == 0 ? 0 : agt.getAgentStatistics().getSentMessages(iter-1);
                iterMsgsSent += (msgNow - msgPrev);

                // Costs
                // todo: check i think that cost takes into account the power consumption of neighbors -
                sumScheduleCost += agt.getAgentStatistics().getScheduleCostIter(iter);
                sumPriceCost    += Utilities.sum(agt.getAgentStatistics().getPriceUSDIter(iter));
                sumPowerCost    += Utilities.sum(agt.getAgentStatistics().getPowerKWhIter(iter));
                avgGain         += agt.getAgentStatistics().getAgentGainIter(iter);
            }
            netLoad += iterMsgsSent;

            res += iter +"\t "
                    + simTime + "\t "
                    + (avgCPtime/agents.size()) + "\t "
                    + (iterMsgsSent/agents.size()) + "\t "
                    + netLoad + "\t "
                    + sumScheduleCost + "\t "
                    + sumPriceCost + "\t "
                    + (sumPriceCost/agents.size()) + "\t "
                    + sumPowerCost + "\t "
                    + (sumPowerCost/agents.size()) + "\t "
                    + (avgGain/agents.size())
                    + "\n";
        }
        return  res;
    }

}
