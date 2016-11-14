package edu.nmsu.Home.Devices;

import java.util.*;

/**
 * Created by nandofioretto on 11/2/16.
 */
public class Actuator extends Device {

    private ArrayList<Action> actions;

    public Actuator(String name, String location) {
        super(name, location);
        actions = new ArrayList<>();
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public Action getAction(int i) {
        return actions.get(i);
    }

    public int getNbActions() {
        return actions.size();
    }

    public void addAction(Action a) {
        actions.add(a);
//        Collections.sort(actions, new Comparator<Action>() {
//            @Override
//            public int compare(Action o1, Action o2) {
//                return o1.getPowerKWh().compareTo(o2.getPowerKWh());
//            }
//        });

        // order actions based on their power consumption
        actions.sort((a1, a2) -> a1.getPowerKWh().compareTo(a2.getPowerKWh()));
    }

    /** Return all the sensor properties affected by this device actions */
    public Set<SensorProperty> getSensorProperties() {
        Set<SensorProperty> sprop = new TreeSet<>();
        for (Action action : actions) {
            for (Action.Effect effect : action.getEffects()) {
                sprop.add(effect.property);
            }
        }
        return sprop;
    }

    @Override
    public String toString() {
        return "Actuator: " + super.toString() + " actions: " + actions;
    }
}
