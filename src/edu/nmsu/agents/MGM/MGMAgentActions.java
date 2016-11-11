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

    Map<Pair<Long, Integer>, MGMAgent.GainMessage> receivedGains = new HashMap<>();


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

    public MGMAgentActions(MGMAgentState agentState) {
        super(agentState);
        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
    }

    public MGMAgentActions(MGMAgentState agentState, long solver_timout, double w_price, double w_power)
    {
        super(agentState);

        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
        solver.setTimeoutMs(solver_timout);
        ((RuleSchedulerSolver)solver).setWeights((int)w_price, (int)w_power);
    }

    // If curr_schedule worst than best schedule: Gain > 0 (50 > 40 = 10)
    // If curr_schedule best than best schedule: Gain < 0 (40 < 50 = -10)
    public void computeGain()
    {
        MGMAgentState state = (MGMAgentState)getAgentState();
        state.setGain(state.getCurrSchedule().getCost() - state.getBestSchedule().getCost());
    }

    public boolean isBestGain(int curr_cycle)
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(curr_cycle);

        double gain = mgmAgentState.getGain();
        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());
            if ( gain > receivedGains.get(agtIdCycle_pair).getGain() ) {
                return false;
            }
        }
        return true;
    }

    public void clearReceivedGains() {
        receivedGains.clear();
    }

    public void updateBestSchedule(RulesSchedule schedule)
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        mgmAgentState.setBestSchedule(schedule);
        mgmAgentState.setCurrSchedule(null);
    }

    public boolean computeSchedule(int curr_cycle)
    {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(curr_cycle);

        long T1, T2;
        T1 = System.currentTimeMillis();

        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());

            Double[] nLoads = receivedGains.get(agtIdCycle_pair).getEnergyProfile();
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

    public boolean computeBaselineSchedule(int curr_cycle) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(curr_cycle);

        long T1, T2;
        T1 = System.currentTimeMillis();

        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());

            Double[] nLoads = receivedGains.get(n.getID()).getEnergyProfile();
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

    public void storeMessage(long id, int curr_cycle, MGMAgent.GainMessage msg)
    {
        Pair p = new Pair(id, curr_cycle);
        if (curr_cycle == msg.getCycle())
            receivedGains.put(p, msg);
    }


    public boolean receivedAllGains(int curr_cycle) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        agtIdCycle_pair.setSecond(curr_cycle);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            agtIdCycle_pair.setFirst(n.getID());
            if (!receivedGains.containsKey(agtIdCycle_pair))
                return false;
        }
        return true;
    }


}
