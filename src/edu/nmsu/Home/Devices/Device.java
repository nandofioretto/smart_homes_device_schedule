package edu.nmsu.Home.Devices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by nandofioretto on 11/2/16.
 */
public class Device {

    String name;
    String location;
    Boolean active;     //! Whether this device is used (inferred from the scheduling Rules)

    public Device(String name, String location) {
        this.name = name;
        this.location = location;
        this.active= false;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public static Device create(String name, JSONObject jsonDevice) {

        try {
            String type = jsonDevice.getString("type");
            if (type.equals("sensor"))
            {
                String location = jsonDevice.getString("location");
                String propStr = jsonDevice.getJSONArray("sensing_properties").getString(0);
                double currState = jsonDevice.getDouble("current_state");
                SensorProperty property = SensorProperty.valueOf(propStr);
                return new Sensor(name, location, property, currState);
            }
            else if (type.equals("actuator"))
            {
                String location = jsonDevice.getString("location");
                Actuator actuator = new Actuator(name, location);

                JSONObject jActions = jsonDevice.getJSONObject("actions");
                Iterator<?> keys = jActions.keys();
                while (keys.hasNext()) {
                    String actionName = (String)keys.next();
                    JSONObject jAction = jActions.getJSONObject(actionName);
                    double actionPower = jAction.getDouble("power_consumed");

                    Action action = new Action(actionName, actionPower);

                    //  Process action effects and deltas
                    JSONArray jEffects = jAction.getJSONArray("effects");
                    for (int i = 0; i < jEffects.length(); i++) {
                        JSONObject jEffect = jEffects.getJSONObject(i);
                        SensorProperty property = SensorProperty.valueOf(jEffect.getString("property"));
                        double delta = jEffect.getDouble("delta");

                        action.addEffect(property, delta);
                    }

                    //  Add action to device
                    actuator.addAction(action);
                }
                return actuator;
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }

        return new Device("none", "");
    }


    @Override
    public String toString() {
        return name + " location: " + location + (active ? " (ON) " : " (OFF) ");
    }

}
