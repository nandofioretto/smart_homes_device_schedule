package edu.nmsu.agents.MGM;

import edu.nmsu.communication.BasicMessage;
import edu.nmsu.communication.ComAgent;
import edu.nmsu.communication.DCOPagent;

import java.util.Arrays;
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

        nbCycles = (int)parameters.get(0);
        long solver_timout = (long)parameters.get(1);
        double w_price  = (double)parameters.get(2);
        double w_power = (double)parameters.get(3);

        view = new MGMAgentView(agentState);
        actions = new MGMAgentActions(agentState, solver_timout, w_price, w_power);

        currentCycle = 0;
    }

    @Override
    protected void onReceive(Object message, ComAgent sender) {
        super.onReceive(message, sender);

        if (message instanceof GainMessage)
        {
            GainMessage msg = (GainMessage) message;
            actions.storeMessage(sender.getId(), msg);
        }
        else if (message instanceof EnergyProfileMessage)
        {
            EnergyProfileMessage msg = (EnergyProfileMessage) message;
            actions.storeMessage(sender.getId(), msg);
        }
    }

    @Override
    protected boolean terminationCondition() {
        return currentCycle >= nbCycles;
    }

    @Override
    protected void onStart()
    {
        // Compute first Schedule
        if (!actions.computeFirstSchedule()) {
            System.err.println(getName() + " Failed computing first schedule");
            System.exit(-1);
        }
        getAgentStatistics().updateIterationStats(view.getCurrentSchedule(), view.getSolvingTimeMs(), view.getGain());

        // Send Energy Profile
        EnergyProfileMessage message =
                new EnergyProfileMessage(currentCycle, view.getCurrentSchedule().getPowerConsumptionKw());
        for (ComAgent neighbor : this.getNeighborsRef() ) {
            neighbor.tell(message, getSelf());
        }

        // Wait to receive En Profiles
        while (!actions.receivedAllEnergyProfiles(currentCycle)) {
            await();
        }

        // Compute current Schedule (and update best one)
        if (actions.computeSchedule(currentCycle)) {
            actions.updateBestSchedule(view.getCurrentSchedule());
        } else {
            System.err.println("Error: Schedule not found!");
        }

        getAgentStatistics().updateIterationStats(view.getBestSchedule(), view.getSolvingTimeMs(), view.getGain());


        while (!terminationCondition()) {
            currentCycle++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MGMcycle();
            getAgentStatistics().updateIterationStats(view.getBestSchedule(), view.getSolvingTimeMs(), view.getGain());
        }
    }

    @Override
    protected void onStop() {
        //System.out.println(view.getBestSchedule().toString());
    }

    public void MGMcycle() {

        // Send Energy Profile of the Best schedule
        EnergyProfileMessage enMessage =
            new EnergyProfileMessage(currentCycle, view.getBestSchedule().getPowerConsumptionKw());
        for (ComAgent neighbor : this.getNeighborsRef() ) {
            neighbor.tell(enMessage, getSelf());
        }

        // Wait to receive En Profiles
        while (!actions.receivedAllEnergyProfiles(currentCycle)) {
            await();
        }

        // Compute current Schedule (and update best one)
        if (actions.computeSchedule(currentCycle)) {

            actions.computeGain();

            // Send Gain message to all neighbors
            GainMessage gainMessage = new GainMessage(currentCycle, view.getGain());
            for (ComAgent neighbor : this.getNeighborsRef() ) {
                neighbor.tell(gainMessage, getSelf());
            }

            // Wait to receive Gains
            while (!actions.receivedAllGains(currentCycle)) {
                await();
            }

            // todo: Check if all gains = 0 --> converged

            // Check if this agent has the best gain: if so   bestSchedule = currentSchedule;
            if (actions.isBestGain(currentCycle)) {
                actions.updateBestSchedule(view.getCurrentSchedule());
            }

        } else {
            System.err.println("Error: Schedule not found!");
        }

    }

    /////////////////////////////////////////////////////////////////////////
    // Messages
    /////////////////////////////////////////////////////////////////////////
    public static class GainMessage extends BasicMessage {
        private final int cycle;
        private final double gain;

        public GainMessage(int cycle, double gain) {
            this.cycle = cycle;
            this.gain = gain;
        }

        public int getCycle() {
            return cycle;
        }

        public double getGain() {
            return gain;
        }

        @Override
        public String toString() {
            return "MGM::GainMessage (" + cycle + "): " + gain;
        }
    }

    public static class EnergyProfileMessage extends BasicMessage {
        private final int cycle;
        private final Double[] energyProfile;

        public EnergyProfileMessage(int cycle, Double[] energyProfile) {
            this.cycle = cycle;
            this.energyProfile = energyProfile.clone();
        }

        public int getCycle() {
            return cycle;
        }

        public Double[] getEnergyProfile() {
            return energyProfile;
        }

        @Override
        public String toString() {
            return "MGM::EnergyProfileMessage (" + cycle + ")" + Arrays.toString(energyProfile);
        }
    }

}
