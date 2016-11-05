package edu.nmsu.Home.LocalSolver;

import edu.nmsu.Home.Devices.Actuator;
import edu.nmsu.problem.Parameters;

import java.util.Arrays;

/**
 * Created by ffiorett on 8/25/16.
 */
public class ActuatorSchedule implements Comparable<ActuatorSchedule> {


    Actuator actuator;
    private int[] schedule;

    public ActuatorSchedule(Actuator device) {
        actuator = device;
        int horizon = Parameters.getHorizon();
        schedule = new int[horizon];
        for (int i = 0; i < horizon; i++) {
            schedule[i] = -1;
        }
    }

    public void setSchedule(int[] schedule) {
        this.schedule = schedule;
    }

    public void setAction(int time, int action_idx) {
        schedule[time] = action_idx;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public int[] getSchedule() {
        return schedule;
    }

    @Override
    public String toString() {
        return "ActuatorSchedule{" +
                "device: " + actuator.getName() +
                ", schedule=" + Arrays.toString(schedule) +
                '}';
    }

    @Override
    public int compareTo(ActuatorSchedule other) {
        return actuator.getName().compareTo(other.actuator.getName());
    }
}
