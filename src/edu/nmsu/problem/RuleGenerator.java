package edu.nmsu.problem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nandofioretto on 11/9/16.
 */
public class RuleGenerator {

    private Random rand = new Random();
    private int horizon = Parameters.getHorizon();


    public RuleGenerator() {
    }

    public JSONObject rule() {
        return null;
    }

    private ArrayList<String> generateLaundryWashRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "LG_WM2016CW" + ((i>0) ? ("_" + i) : "");
        String property = "laundry_wash";

        int state = Utilities.genRand(1, 2);
        int time = Utilities.genRand(state+1, horizon);

        rules.add("1\t" + device + "\t" + property + "\teq\t" + state + "\tbefore\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t" + state);
        return rules;
    }

    private ArrayList<String> generateLaundryDryRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "GE_WSM2420D3WW" + ((i>0) ? ("_" + i) : "");
        String property = "laundry_dry";

        int state = Utilities.genRand(2, 3);
        int time = Utilities.genRand(state+1, horizon);

        rules.add("1\t" + device + "\t" + property + "\teq\t" + state + "\tbefore\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t" + state);
        return rules;
    }

    private ArrayList<String> generateDishWashRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Kenmore_665.13242K900" + ((i>0) ? ("_" + i) : "");
        String property = "dish_wash";

        String timePred = Utilities.genRand(new String[]{"before", "after"});

        int state = Utilities.genRand(1, 2);
        int time;

        if (timePred.compareTo("before") == 0) {
            time = Utilities.genRand(state+2, horizon);
        } else {
            time = Utilities.genRand(1, horizon - state-2);
        }

        rules.add("1\t" + device + "\t" + property + "\teq\t" + state + "\t" + timePred + "\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t" + state);
        return rules;
    }

    private ArrayList<String> generateBakeRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Kenmore_790.91312013" + ((i>0) ? ("_" + i) : "");
        String property = "bake";

        String timePred = "at";

        int state = Utilities.genRand(1, 3);
        int time  = Utilities.genRand(state + 3, horizon - 3);

        rules.add("1\t" + device + "\t" + property + "\teq\t" + state + "\t" + timePred + "\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t" + state);
        return rules;
    }

    private ArrayList<String> generateWaterTempRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "water_tank" + ((i>0) ? ("_" + i) : "");
        String property = "water_temp";

        String timePred = "within";

        int state1 = Utilities.genRand(26, 30);
        int state2 = Utilities.genRand(state1+12, 45);
        int time1  = Utilities.genRand(1, horizon - 6);
        int time2  = Utilities.genRand(time1 + 3, horizon);

        rules.add("1\t" + device + "\t" + property + "\tgeq\t" + state1 + "\t" + timePred + "\t" + time1 + "\t" + time2);
        rules.add("1\t" + device + "\t" + property + "\tleq\t" + state2 + "\t" + timePred + "\t" + time1 + "\t" + time2);

        rules.add("0\t" + device + "\t" + property + "\tgeq\t18");
        rules.add("0\t" + device + "\t" + property + "\tleq\t50");
        return rules;
    }

    private ArrayList<String> generateEVRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "Tesla_S" + ((i>0) ? ("_" + i) : "");
        String property = "charge";

        String timePred = Utilities.genRand(new String[]{"before", "after"});
        int state = Utilities.genRand(new int[] {60, 80, 100});
        int time;

        if (timePred.compareTo("before") == 0) {
            time = Utilities.genRand(5, horizon);
        } else {
            time = Utilities.genRand(1, horizon - 5);
        }

        rules.add("1\t" + device + "\t" + property + "\tgeq\t" + state + "\t" + timePred + "\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t100");
        return rules;
    }

    private ArrayList<String> generateTempHeatRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "room" + ((i>0) ? ("_" + i) : "");
        String property = "temperature_heat";
        String timePred = "within";

        int state1 = Utilities.genRand(18, 25);
        int state2 = Utilities.genRand(state1+10, 30);
        int time1  = Utilities.genRand(1, horizon - 6);
        int time2  = Utilities.genRand(time1 + 3, horizon);

        rules.add("1\t" + device + "\t" + property + "\tgeq\t" + state1 + "\t" + timePred + "\t" + time1 + "\t" + time2);
        rules.add("1\t" + device + "\t" + property + "\tleq\t" + state2 + "\t" + timePred + "\t" + time1 + "\t" + time2);

        rules.add("0\t" + device + "\t" + property + "\tgeq\t18");
        rules.add("0\t" + device + "\t" + property + "\tleq\t50");
        return rules;
    }

    private ArrayList<String> generateCleanlinessRules(int i) {
        ArrayList<String> rules = new ArrayList<>();
        String device = "room" + ((i>0) ? ("_" + i) : "");
        String property = "cleanliness";

        String timePred = "before";
        int state = Utilities.genRand(50, 99);
        int time = Utilities.genRand(horizon-6, horizon);

        rules.add("1\t" + device + "\t" + property + "\tgeq\t" + state + "\t" + timePred + "\t" + time);
        rules.add("0\t" + device + "\t" + property + "\tgeq\t0");
        rules.add("0\t" + device + "\t" + property + "\tleq\t100");
        return rules;
    }

}
