package edu.nmsu.Home.Rules;

import edu.nmsu.Home.Devices.Actuator;
import edu.nmsu.Home.Devices.Sensor;

import java.util.ArrayList;

/**
 * Created by ffiorett on 8/21/16.
 *
 * Not really a Factory class - it serves to create the list of predictive models, given
 * a sensro and all the possible Devices.
 */
public class PredictiveModelFactory {

    public static PredictiveModel create(Sensor sensor, ArrayList<Actuator> actuators, String agtName) {
        ArrayList<Actuator> modelActuators = new ArrayList<>();

        for (Actuator a : actuators) {
            if ( (a.getLocation().equals(sensor.getLocation()) ||
                    sensor.getLocation().equals(a.getName())) &&
                a.getSensorProperties().contains(sensor.getSensorProperty())) {
                modelActuators.add(a);
            }
        }
        if (modelActuators.size() > 0)
            return new PredictiveModel(sensor, modelActuators, agtName);
        else {
            return null;
        }
    }

}
