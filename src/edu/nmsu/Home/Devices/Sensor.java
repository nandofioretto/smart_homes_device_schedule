package edu.nmsu.Home.Devices;

/**
 * Created by nandofioretto on 11/2/16.
 */
public class Sensor extends Device {

    private SensorProperty sensorProperty;
    /** This is a measurement on the current state sensed by this device */
    double  currentState = 0;

    public Sensor(String name, String location, SensorProperty sensorProperty, double measurement) {
        super(name, location);
        this.sensorProperty = sensorProperty;
        this.currentState = measurement;
    }

    public SensorProperty getSensorProperty() {
        return sensorProperty;
    }

    public double getCurrentState() {
        return currentState;
    }

    public void updatedState(double newState) {
        currentState = newState;
    }

    @Override
    public String toString() {
        return "Sensor: " + super.toString() + " property: " + sensorProperty;
    }
}
