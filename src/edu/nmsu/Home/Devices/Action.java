package edu.nmsu.Home.Devices;

import java.util.ArrayList;

/** The device Action */
public class Action {

    private String name;
    private double powerKWh;
    private ArrayList<Effect> effects;

    public Action(String name, double powerKWh) {
        this.name = name;
        this.powerKWh = powerKWh;
        effects = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public Double getPowerKWh() {
        return powerKWh;
    }

    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public boolean contains(SensorProperty property) {
        for (Effect e : effects) {
            if (e.property == property)
                return true;
        }
        return false;
    }

    /** Returns the delta effect associated to the sensor property \p property or 0 otherwise */
    public double getDeltaOf(SensorProperty property) {
        for (Effect e : effects) {
            if (e.property == property)
                return e.delta;
        }
        return 0;
    }

    public void addEffect(SensorProperty propery, double delta) {
        effects.add(new Effect(propery, delta));
    }

    @Override
    public String toString() {
        return "Action: " + name +
                ", powerKWh=" + powerKWh +
                ", effects=" + effects;
    }

    /**
     * Action Effects
     */
    public class Effect {
        public SensorProperty property;
        public double delta;

        public Effect(SensorProperty property, double delta) {
            this.property = property;
            this.delta = delta;
        }

        @Override
        public String toString() {
            return "(" + property +  ", delta=" + delta + ')';
        }
    }

}
