package edu.nmsu.agents.MGM;

import edu.nmsu.communication.ComAgent;
import edu.nmsu.communication.DCOPagent;
import edu.nmsu.kernel.AgentState;

import java.util.List;

/**
 * Created by nandofioretto on 11/1/16.
 */
public class MGMAgent extends DCOPagent {

    public MGMAgent(ComAgent statsCollector, AgentState agentState, List<Object> parameters) {
            super(statsCollector, agentState.getName(), agentState.getID());
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
    }


    @Override
    protected boolean terminationCondition() {
        return true;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onStop() {
    }

}
