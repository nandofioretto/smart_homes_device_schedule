package edu.nmsu.agents.MGM;

import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentState;
import edu.nmsu.kernel.AgentView;

import java.util.ArrayList;

/**
 * Created by nandofioretto on 11/6/16.
 */
public class MGMAgentView extends AgentView {

    public MGMAgentView(MGMAgentState agentState) {
        super(agentState);
    }

    public ArrayList<AgentState> getNeighbors() {
        return agentState.getNeighbors();
    }

    public RulesSchedule getCurrentSchedule() {
        return ((MGMAgentState)agentState).getHomeSchedule();
    }

    public double[] getNeighborsLoad() {
        return ((MGMAgentState)agentState).getNeighborLoads();
    }
}
