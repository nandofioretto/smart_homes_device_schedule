package edu.nmsu.Home.Rules;

import edu.nmsu.Home.Devices.Actuator;
import edu.nmsu.Home.Devices.Sensor;
import edu.nmsu.Home.Devices.SensorProperty;
import edu.nmsu.problem.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffiorett on 8/18/16.
 * This is the model of an environment. A prediction model is associated to a sensor (s), and a set of
 * actuators \Phi_s = {a_1, \ldots, a_n}, which may  have a direct effect on the environment modelled by
 * this Prediction model.
 * Each actuator a_i \in \Phi_s is such that its location is equal to the location of the sensor,
 * and so that it has at least one state in its possible states which is associated to the property of the sensor.
 */
public class PredictiveModel
{
    // Correspond to location and sensor property of the associated sensor.
    private String location = "";
    private SensorProperty property;
    // The sensor associated to the prediction model
    private Sensor sensor;
    // The actuators associated to this model.
    // Make sure that the simluated Devices return an integral delta
    private ArrayList<Actuator> actuators;

    Boolean active;     //! Whether this predictive model is used (inferred from the scheduling Rules)


    public PredictiveModel(Sensor sensor, ArrayList<Actuator> actuators, String agtName) {
        this.sensor = sensor;
        this.actuators = actuators;
        this.location = sensor.getLocation();
        this.property = sensor.getSensorProperty();
        this.active  = false;

        // Check - actuator and sensor in same location; and sensor property of actuators = sensor property
        for (Actuator a : actuators) {
            assert(a.getLocation() == this.location);
            assert(a.getSensorProperties().contains(this.property));
        }

        // Save element in map
        //map.put(new Pair<>(location, property), this);
        map.put(new Pair<>(agtName, new Pair<>(location, property)), this);
    }

    public String getLocation() {
        return location;
    }

    public SensorProperty getProperty() {
        return property;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public ArrayList<Actuator> getActuators() {
        return actuators;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
        sensor.setActive(true);
        for (Actuator a: actuators)
            a.setActive(true);
    }


    @Override
    public String toString() {
        String ret = "PredictiveModel{" +
                "property=" + property +
                ", location='" + location + '\'' +
                ", sensor=" + sensor.getName() +
                ", actuators=[";
        for (Actuator a : actuators) {
            ret += a.getName() + " ";
        }
        ret += (active ? " (ON) " : " (OFF) ");
        ret += "]}";
        return ret;
    }

    /**returns a more easily readable string for debugging purposes*/
    public String toRead() {
        String ret = "{sp[" + property;
        while(ret.length() <= 20) ret += " ";
        ret = ret + "], loc[" + location;
        while(ret.length() <= 39) ret += " ";
        ret += "]}: ";

        return ret;
    }

    // -------------------------------------------
    // Static Member Definition (MAP)
    // -------------------------------------------

    public static PredictiveModel valueOf(String agtName, String location, SensorProperty property) {
        return map.get(new Pair<>(agtName, new Pair<>(location, property)));
    }

    //private static Map<Pair<String, SensorProperty>, PredictiveModel> map = new HashMap<>();

    private static Map<Pair<String, Pair<String, SensorProperty>>, PredictiveModel> map = new HashMap<>();

}
