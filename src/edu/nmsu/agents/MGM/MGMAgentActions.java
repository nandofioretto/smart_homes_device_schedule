package edu.nmsu.agents.MGM;

import edu.nmsu.Home.Home;
import edu.nmsu.Home.LocalSolver.CPSolver;
import edu.nmsu.Home.LocalSolver.RuleSchedulerSolver;
import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentActions;
import edu.nmsu.kernel.AgentState;
import edu.nmsu.problem.Pair;
import edu.nmsu.problem.Parameters;
import edu.nmsu.problem.Utilities;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nandofioretto on 11/6/16.
 */

public class MGMAgentActions extends AgentActions {

    CPSolver solver;

    Map<Pair<Long, Integer>, Double> receivedGains = new HashMap<>();
    Map<Pair<Long, Integer>, Double[]> receivedEnergyProfiles = new HashMap<>();

    // For each cycle
    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b){
            return;
        }
        @Override
        public void write(byte[] b){
            return;
        }
        @Override
        public void write(byte[] b, int off, int len){
            return;
        }
        public NullOutputStream(){
        }
    };
    private PrintStream nullStream = new PrintStream(new NullOutputStream());
    private PrintStream outStream = System.out;

    Pair<Long, Integer> agtIdCycle_pair = new Pair(0,0);


    public MGMAgentActions(MGMAgentState agentState, long solver_timout, double w_price, double w_power)
    {
        super(agentState);

        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
        solver.setTimeoutMs(solver_timout);
        ((RuleSchedulerSolver)solver).setWeights((int)w_price, (int)w_power);

    }

    public void computeGain()
    {
        MGMAgentState state = (MGMAgentState)getAgentState();
        state.setGain( Math.max(0,  state.getBestSchedule().getCost() - state.getCurrSchedule().getCost()) );
    }

    public boolean isBestGain(int curr_cycle)
    {
        MGMAgentState state = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(curr_cycle);

        double gain = state.getGain();

        for (AgentState n : state.getNeighbors())
        {
            agtIdCycle_pair.setFirst(n.getID());
            double neighborGain = receivedGains.get(agtIdCycle_pair);

            if ( gain < neighborGain) {
                return false;
            } else if (gain == neighborGain && state.getID() > n.getID()) {
                return false;
            }
        }
        return true;
    }

    public void updateBestSchedule(RulesSchedule schedule)
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        mgmAgentState.setBestSchedule(schedule);
    }

    public boolean computeSchedule(int cycleNo)
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(cycleNo);

        long T1, T2;
        T1 = System.currentTimeMillis();

        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());

            Double[] nLoads = receivedEnergyProfiles.get(agtIdCycle_pair);
            neighborLoads = Utilities.sum(neighborLoads, nLoads);
        }

        System.setOut(nullStream);
        System.setErr(nullStream);
        RulesSchedule rs = solver.getSchedule(neighborLoads);
        mgmAgentState.setCurrSchedule(rs);
        System.setOut(outStream);
        System.setErr(outStream);

        T2 = System.currentTimeMillis();
        mgmAgentState.setSolvingTimeMs(T2 - T1);

        return (rs.getCost() != Double.MAX_VALUE);
    }

    public boolean computeFirstSchedule()
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        long T1, T2;
        T1 = System.currentTimeMillis();

        System.setOut(nullStream);
        RulesSchedule rs = solver.getFirstSchedule();
        mgmAgentState.setCurrSchedule(rs);
        System.setOut(outStream);

        T2 = System.currentTimeMillis();
        mgmAgentState.setSolvingTimeMs(T2 - T1);

        return (rs.getCost() != Double.MAX_VALUE);
    }

    public boolean computeBaselineSchedule(int cycleNo) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(cycleNo);

        long T1, T2;
        T1 = System.currentTimeMillis();

        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());

            Double[] nLoads = receivedEnergyProfiles.get(n.getID());
            neighborLoads = Utilities.sum(neighborLoads, nLoads);
        }

        System.setOut(nullStream);
        RulesSchedule rs = solver.getBaselineSchedule(neighborLoads);
        mgmAgentState.setCurrSchedule(rs);
        System.setOut(outStream);

        T2 = System.currentTimeMillis();
        mgmAgentState.setSolvingTimeMs(T2 - T1);

        return (rs.getCost() != Double.MAX_VALUE);
    }

    public void storeMessage(long id, MGMAgent.GainMessage msg)
    {
        Pair p = new Pair(id, msg.getCycle());
        receivedGains.put(p, msg.getGain());
    }

    public void storeMessage(long id, MGMAgent.EnergyProfileMessage msg)
    {
        Pair p = new Pair(id, msg.getCycle());
        receivedEnergyProfiles.put(p, msg.getEnergyProfile());
    }


    /** Check if the agent received gains from all neighbors at iteration \p iter */
    public boolean receivedAllGains(int iter) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(iter);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());
            if (!receivedGains.containsKey(agtIdCycle_pair))
                return false;
        }
        return true;
    }

    /** Check if the agent received energey profiles from all neighbors at iteration \p iter */
    public boolean receivedAllEnergyProfiles(int iter) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(iter);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());
            if (!receivedEnergyProfiles.containsKey(agtIdCycle_pair))
                return false;
        }
        return true;
    }



}
