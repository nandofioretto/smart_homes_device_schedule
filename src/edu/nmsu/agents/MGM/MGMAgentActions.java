package edu.nmsu.agents.MGM;

import edu.nmsu.Home.Home;
import edu.nmsu.Home.LocalSolver.CPSolver;
import edu.nmsu.Home.LocalSolver.RuleSchedulerSolver;
import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentActions;
import edu.nmsu.kernel.AgentState;
import edu.nmsu.problem.Parameters;
import edu.nmsu.problem.Utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nandofioretto on 11/6/16.
 */

public class MGMAgentActions extends AgentActions {

    CPSolver solver;

    Map<Long, MGMAgent.GainMessage> receivedGains = new HashMap<>();

    public MGMAgentActions(MGMAgentState agentState) {
        super(agentState);
        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
    }

    public MGMAgentActions(MGMAgentState agentState, long solver_timout, double w_price, double w_power) {
        super(agentState);

        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
        solver.setTimeoutMs(solver_timout);
        ((RuleSchedulerSolver)solver).setWeights((int)w_price, (int)w_power);
    }

    // If curr_schedule worst than best schedule: Gain > 0 (50 > 40 = 10)
    // If curr_schedule best than best schedule: Gain < 0 (40 < 50 = -10)
    public double computeGain() {
        MGMAgentState state = (MGMAgentState)getAgentState();
        return (state.getCurrSchedule().getUtility() - state.getBestSchedule().getUtility());
    }

    public boolean isBestGain() {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());

        double gain = computeGain();
        for (AgentState n : mgmAgentState.getNeighbors()) {
            if ( gain > receivedGains.get(n.getID()).getGain() ) {
                return false;
            }
        }
        return true;
    }

    public void clearReceivedGains() {
        receivedGains.clear();
    }

    public void updateBestSchedule(RulesSchedule schedule) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        mgmAgentState.setBestSchedule(schedule);
        mgmAgentState.setCurrSchedule(null);
    }

    public boolean computeSchedule() {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            double[] nLoads = receivedGains.get(n.getID()).getEnergyProfile();
            neighborLoads = Utilities.sum(neighborLoads, nLoads);
        }

        RulesSchedule rs = solver.getSchedule(neighborLoads);
        mgmAgentState.setCurrSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }

    public boolean computeFirstSchedule() {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        RulesSchedule rs = solver.getFirstSchedule();
        mgmAgentState.setCurrSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }

    public boolean computeBaselineSchedule() {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());

        // Sum neighbors loads
        double[] neighborLoads = new double[Parameters.getHorizon()];
        Arrays.fill(neighborLoads, 0);

        for (AgentState n : mgmAgentState.getNeighbors()) {
            double[] nLoads = receivedGains.get(n.getID()).getEnergyProfile();
            neighborLoads = Utilities.sum(neighborLoads, nLoads);
        }

        RulesSchedule rs = solver.getBaselineSchedule(neighborLoads);
        mgmAgentState.setCurrSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }

    public void storeMessage(long id, int curr_cycle, MGMAgent.GainMessage msg) {
        if (curr_cycle == msg.getCycle())
            receivedGains.put(id, msg);
    }

    public boolean receivedAllGains(int curr_cycle) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());

        for (AgentState n : mgmAgentState.getNeighbors()) {
            if (!receivedGains.containsKey(n.getID()))
                return false;
        }
        return true;
    }


}
