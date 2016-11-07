package edu.nmsu.kernel;

/**
 * Created by ffiorett on 8/1/15.
 * Defines the actions of the agent which can modify the agent state.
 */
public class AgentActions {

    private AgentState agentState;

    public AgentActions(AgentState agentState) {
        this.agentState = agentState;
    }

    public AgentState getAgentState() {
        return agentState;
    }

    public void setVariableValue(int value) {
        agentState.getVariable().setValue(value);
    }

    public void setVariableValue(int varIdx, int value) {
        agentState.getVariable(varIdx).setValue(value);
    }

    public void setVariableVariableAtRandom() {
        agentState.getVariable().setValueAtRandom();
    }

    public void setVariableVariableAtRandom(int varIdx) {
        agentState.getVariable(varIdx).setValueAtRandom();
    }

}
