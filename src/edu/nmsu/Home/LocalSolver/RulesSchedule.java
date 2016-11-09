package edu.nmsu.Home.LocalSolver;

import edu.nmsu.problem.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffiorett on 8/24/16.
 */
public class RulesSchedule  {

    /** Map device ID with its schedule. */
    private Map<String, ActuatorSchedule> mapSchedules = new HashMap<>();

    /** The payoff associated to this schedule instance. */
    private double utility;

    /** Is the aggregated (=all actuators) power consumption in KW (of each time step) */
    private double[] powerConsumptionKw;

    /** Is the price paied for this schedule per time step */
    private double[] pricePerTimeStep;

    /** Constructor that creates a schedule with an array list of schedule entries */
    public RulesSchedule() {
        int timesteps = Parameters.getHorizon();
        powerConsumptionKw = new double[timesteps];
        pricePerTimeStep   = new double[timesteps];
        utility = 0;
    }

    public RulesSchedule(Object obj) {
        RulesSchedule temp = null;
        if (obj instanceof RulesSchedule) {
            temp = (RulesSchedule) obj;
        }

        int timesteps = Parameters.getHorizon();
        this.utility   = temp.getUtility();
        this.powerConsumptionKw = new double[timesteps];
        for (int i = 0; i < timesteps; i++) {
            this.powerConsumptionKw[i] = temp.getPowerConsumptionKw()[i];
        }
    }

    /** Inserts an Actuator schedule in the object map */
    public void insert(ActuatorSchedule schedule) {
        mapSchedules.put(schedule.getActuator().getName(), schedule);
    }

    /** Returns the actuator schedule associated to the actuator Name */
    public ActuatorSchedule get(String actuatorName) {
        return mapSchedules.get(actuatorName);
    }

    /** The vector of power consumptions */
    public double[] getPowerConsumptionKw() {
        return powerConsumptionKw;
    }

    /** Inserts a power consumption 'power' for time 'time' */
    public void setPowerConsumptionKw(int time, double power) {
        powerConsumptionKw[time] = power;
        pricePerTimeStep[time] = power * Parameters.getPriceSchema()[time];
    }

    /** Returns the price cost of this schedule */
    public double getSchedulePrice() {
        double sum = 0;
        for (double price_t : pricePerTimeStep) sum += price_t;
        return sum;
    }

    /** Returns the price cost of this schedule per time steps */
    public double[] getPricePerTimeStep() {
        return pricePerTimeStep;
    }

    /** Method get the schedule cost associated to the current schedule */
    public double getUtility() {return utility; }

    /** Method to set the current schedule cost to a new schedule cost */
    public void setUtility(double utility) { this.utility = utility; }

    /** method to get the schedule entries for the schedule */
    public Collection<ActuatorSchedule> getSchedule() {
        return mapSchedules.values();
    }

    @Override
    public String toString() {
        String ret = "";
        for (ActuatorSchedule s : mapSchedules.values()) {
            ret += s.toString() + "\n";
        }
        ret +=  "\tutility: " + utility + "\n";
        ret +=  "\tpowerConsumptionKw: " + Arrays.toString(powerConsumptionKw) + "\n";
        ret +=  "\tpricePerTimeStep: " + Arrays.toString(pricePerTimeStep) + "\n";
/*
        int sumDeltaPower = 0;
        double totalCost     = 0.0;
        for (int t1 = 0; t1 < LPP.getTimeSteps(); t1++) {
            for (int t2 = t1 + 1; t2 < LPP.getTimeSteps(); t2++) {
                sumDeltaPower += Math.abs(powerConsumptionKw[t1] - powerConsumptionKw[t2]);
            }
            totalCost += pricePerTimeStep[t1];
        }

        ret += "\tsumDeltaPower: " + sumDeltaPower + "\n";
        ret += "\ttotalCost: "     + totalCost     + "\n";
*/

        return ret;
    }
}
