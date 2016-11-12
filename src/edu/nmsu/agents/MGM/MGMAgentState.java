package edu.nmsu.agents.MGM;

import edu.nmsu.Home.Home;
import edu.nmsu.Home.LocalSolver.RulesSchedule;
import edu.nmsu.kernel.AgentState;
import edu.nmsu.problem.Parameters;

/**
 * Created by nandofioretto on 11/6/16.
 */
public class MGMAgentState extends AgentState {

    /** The home this agent controls */
    private Home home;
    private final int timeHorizon = Parameters.getHorizon();
    private double[] backgroundLoads = new double[timeHorizon]; // all zeros
    private double[] neighborLoads = new double[timeHorizon]; // all zeros
    private RulesSchedule bestSchedule = new RulesSchedule();
    private RulesSchedule currSchedule = new RulesSchedule();
    private long solvingTimeMs = 0;     // time spend by the scheduler in current iteration
    private double gain = -1;

    public MGMAgentState(String name, long ID, Home pHome, double[] bgLoads) {
        super(name, ID);
        this.home = pHome;

        for (int j = 0; j < neighborLoads.length; j++)
            neighborLoads[j] = 0;
        for (int j = 0; j < backgroundLoads.length; j++)
            backgroundLoads[j] = bgLoads[j];

//        registerVariable(new IntVariable(name, ID, 0,0));
//        registerConstraint(new TableBinaryConstraint());
    }

    public Home getHome() {
        return home;
    }

    public RulesSchedule getCurrSchedule() {
        return currSchedule;
    }

    public void setCurrSchedule(RulesSchedule currSchedule) {
        this.currSchedule = currSchedule;
    }

    public RulesSchedule getBestSchedule() {
        return bestSchedule;
    }

    public long getSolvingTimeMs() {
        return solvingTimeMs;
    }

    public void setSolvingTimeMs(long solvingTimeMs) {
        this.solvingTimeMs = solvingTimeMs;
    }

    public void setBestSchedule(RulesSchedule bestSchedule) {
        this.bestSchedule = bestSchedule;
    }

    public double getGain() {
        return gain;
    }

    public void setGain(double gain) {
        this.gain = gain;
    }

    public double[] getBackgroundLoads() {
        return backgroundLoads;
    }

    public double[] getNeighborLoads() {
        return neighborLoads;
    }

    public void setBackgroundLoads(double[] backgroundLoads) {
        this.backgroundLoads = backgroundLoads;
    }
}
