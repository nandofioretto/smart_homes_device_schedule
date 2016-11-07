package edu.nmsu.agents.MGM;

import edu.nmsu.communication.ComAgent;
import edu.nmsu.communication.DCOPagent;

import java.util.List;

/**
 * Created by nandofioretto on 11/1/16.
 */
public class MGMAgent extends DCOPagent {

    private int nbCycles;
    private int currentCycle;

    private MGMAgentView view;
    private MGMAgentActions actions;

    /**
     * @param statsCollector
     * @param agentState
     * @param parameters 1. Number of MGM Cycles
     *                   2. Timeout for CP-solver
     *                   3. weight for first objective (w_cost)
     *                   4. weight for second objective (w_power)
     */
    public MGMAgent(ComAgent statsCollector, MGMAgentState agentState, List<Object> parameters) {
            super(statsCollector, agentState.getName(), agentState.getID());

        // view = new MGMAgentView(agentState);
        actions = new MGMAgentActions(agentState);

        nbCycles = (int)parameters.get(0);
        long solver_timout = (long)parameters.get(1);
        double w_cost  = (double)parameters.get(2);
        double w_power = (double)parameters.get(3);
        currentCycle = 0;
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
    }


    @Override
    protected boolean terminationCondition() {
        return currentCycle >= nbCycles;
    }

    @Override
    protected void onStart() {
        actions.computeFirstSchedule();

        // Send messages to their neighbors
        while (!terminationCondition()) {
            currentCycle++;
            MGMcycle();
        }
    }

    @Override
    protected void onStop() {
    }

    public void MGMcycle() {

        for (ComAgent a : this.getNeighborsRef() ) {
//                a.tell(new RandomDestroy.ContextMessage(selfRef.getAgentView().isVarDestroyed(),
//                        selfRef.getAgentView().getVariableValue()), selfRef);
        }

//        while (nbContextMsgReceived < selfRef.getNeighborsRef().size()) {
//            selfRef.await();
    // await = check mailbox and process new message
//        }



    }

}
