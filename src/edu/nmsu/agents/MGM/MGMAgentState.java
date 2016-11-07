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
    private RulesSchedule homeSchedule;

    public MGMAgentState(String name, long ID, Home pHome, double[] bgLoads) {
        super(name, ID);
        this.home = pHome;

        for (int j = 0; j < neighborLoads.length; j++)
            neighborLoads[j] = 0;
        for (int j = 0; j < backgroundLoads.length; j++)
            backgroundLoads[j] = bgLoads[j];
    }

    public Home getHome() {
        return home;
    }

    public RulesSchedule getHomeSchedule() {
        return homeSchedule;
    }

    public void setHomeSchedule(RulesSchedule homeSchedule) {
        this.homeSchedule = homeSchedule;
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
