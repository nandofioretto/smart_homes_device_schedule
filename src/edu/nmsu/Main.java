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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Main {

    public static void execute(String file) {
        List<Object> algParams = new ArrayList<>();

        int nbIterations = 1;
        long solverTimeoutMs = 10000;
        double wCost = 1;
        double wPower = 1;

        DCOPInstance dcopInstance = DCOPInstanceFactory.importDCOPInstance(file);
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
        for (int i=0; i < 1; i++) {
            String file = "resources/inputs/1cluster/instance_DM_"+i;//+".json";
//            generateSHDSInstances(file, 5);
//            execute(file);
            generateMultiSHDSInstances(file, 6, new int[] {10, 20, 100});
        }
        return;
    }


    public static void generateMultiSHDSInstances(String fileName, int nDevices, int[] radious) {
        ArrayList<Topology> topologies = new ArrayList<>();
        for (int r : radious) {
            topologies.add(new Topology(500, 100, r));
        }
        RuleGenerator ruleGen = new RuleGenerator();
        GeneratorMulti gen = new GeneratorMulti(topologies, ruleGen, nDevices);

        for (int i = 0; i < radious.length; i++) {
            JSONObject exp = gen.generate(i);

            try {
                FileWriter fileOut = new FileWriter(fileName + "_" + radious[i] + ".json");
                fileOut.write(exp.toString(2));
                fileOut.flush();
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void generateSHDSInstances(String fileName, int nDevices) {
        Topology topo = new Topology(500, 100, 100);
        //Topology topo = new Topology(718, 100, 100);
        //Topology topo = new Topology(3766, 100, 100);
        RuleGenerator ruleGen = new RuleGenerator();
        Generator gen = new Generator(topo, ruleGen, nDevices);

        JSONObject exp = gen.generate();

        try {
            FileWriter fileOut = new FileWriter(fileName);
            fileOut.write(exp.toString(2));
            fileOut.flush();
            fileOut.close();
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
                + "vgGain\t";
        for (int i = 0; i < Parameters.getHorizon(); i++)
            res += "pw_t_" + i + ((i == Parameters.getHorizon()- 1) ? "\n" : "\t");


        DecimalFormat df = new DecimalFormat("#.00");

        int maxIter = DCOPinfo.leaderAgent.getAgentStatistics().size();
        long simTime = 0; int netLoad = 0;

        double[] aggrPower = new double[Parameters.getHorizon()];

        for (int iter = 0; iter < maxIter; iter++) {
            int iterMsgsSent = 0;
            double sumScheduleCost = 0;
            double sumPriceCost    = 0;
            double sumPowerCost    = 0;
            double avgGain         = 0;
            double avgCPtime       = 0;

            for (int i = 0; i < aggrPower.length; i++) aggrPower[i] = 0;

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

                // Power
                aggrPower = Utilities.sum(aggrPower, agt.getAgentStatistics().getPowerKWhIter(iter));
            }
            netLoad += iterMsgsSent;

            res += iter +"\t "
                    + df.format(simTime) + "\t "
                    + df.format(avgCPtime/agents.size()) + "\t "
                    + df.format(iterMsgsSent/agents.size()) + "\t "
                    + df.format(netLoad) + "\t "
                    + df.format(sumScheduleCost) + "\t "
                    + df.format(sumPriceCost) + "\t "
                    + df.format(sumPriceCost/agents.size()) + "\t "
                    + df.format(sumPowerCost) + "\t "
                    + df.format(sumPowerCost/agents.size()) + "\t "
                    + df.format(avgGain/agents.size()) + "\t ";
            for (int i = 0; i < aggrPower.length; i++)
                res += df.format(aggrPower[i]) + ((i == aggrPower.length - 1) ? "\n" : "\t");
        }
        return  res;
    }



}
