package edu.nmsu.agents.MGM;

import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentView;

/**
 * Created by nandofioretto on 11/6/16.
 */
public class MGMAgentView extends AgentView {

    public MGMAgentView(MGMAgentState agentState) {
        super(agentState);
    }

    public RulesSchedule getCurrentSchedule() {
        return ((MGMAgentState)agentState).getCurrSchedule();
    }

    public double[] getNeighborsLoad() {
        return ((MGMAgentState)agentState).getNeighborLoads();
    }

    public long getSolvingTimeMs() {
        return ((MGMAgentState)agentState).getSolvingTimeMs();
    }

    public double getGain() {
        return ((MGMAgentState)agentState).getGain();
    }

    public RulesSchedule getBestSchedule() {
        return ((MGMAgentState)agentState).getBestSchedule();
    }

}
