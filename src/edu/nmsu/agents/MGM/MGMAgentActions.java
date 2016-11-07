package edu.nmsu.agents.MGM;

import edu.nmsu.Home.Home;
import edu.nmsu.Home.LocalSolver.CPSolver;
import edu.nmsu.Home.LocalSolver.RuleSchedulerSolver;
import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentActions;

/**
 * Created by nandofioretto on 11/6/16.
 */

public class MGMAgentActions extends AgentActions {

    CPSolver solver;

    public MGMAgentActions(MGMAgentState agentState) {
        super(agentState);

        Home home = agentState.getHome();
        assert (home != null);
        solver = new RuleSchedulerSolver( home.getName(), home.getSchedulingRules(), agentState.getBackgroundLoads());
    }

    /**
     *
     * @param neighborLoads An array of the aggregated power counsumption of the neighbors.
     *                      of size HORIZON.
     * @return
     */
    public boolean computeDeviceSchedule(double[] neighborLoads) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        RulesSchedule rs = solver.getSchedule(neighborLoads);
        mgmAgentState.setHomeSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }

    public boolean computeFirstSchedule() {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        RulesSchedule rs = solver.getFirstSchedule();
        mgmAgentState.setHomeSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }

    public boolean computeBaselineSchedule(double[] neighborLoads) {
        MGMAgentState mgmAgentState = ((MGMAgentState)getAgentState());
        RulesSchedule rs = solver.getBaselineSchedule(neighborLoads);
        mgmAgentState.setHomeSchedule(rs);
        return (rs.getUtility() != Double.MAX_VALUE);
    }
}
