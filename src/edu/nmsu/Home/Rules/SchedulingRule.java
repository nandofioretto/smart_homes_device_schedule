package edu.nmsu.Home.Rules;

import edu.nmsu.Home.Devices.SensorProperty;
import edu.nmsu.problem.Parameters;

/**
 * Created by ffiorett on 8/19/16.
 * Example of a scheduling rule:
 * <type> <location> <property> <relation> <goal_state> <time_prefix> <time_value>
 */
public class SchedulingRule {
    private RuleType type;
    private String location;
    private SensorProperty property;
    private RuleBinaryRelation  relation;
    private int goalState;
    private RuleTimePrefix timePrefix;
    private int timeStart;
    private int timeEnd;
    private RuleTimePredicate predicate;
    private PredictiveModel predictiveModel;

    private Boolean active;  // whether this rule is active

    /**
     * Constructs a Rule given a string describing the Rule BNF.
     * @param rule A string with the RULE BNF
     */
    public SchedulingRule(String rule, String agtName) {

        String[] ruleElements = rule.split("[ \t]+");
        if(ruleElements[0].equals("")){ //(just in case " " was in front of first element)
            for(int i = 0; (i+1) < ruleElements.length; i++) {
                ruleElements[i] = ruleElements[i+1];
            }
        }

        // Link Rule Elements
        type      = RuleType.valueOf(Integer.parseInt(ruleElements[0]));
        location  = ruleElements[1];
        property  = SensorProperty.valueOf(ruleElements[2]);
        relation  = RuleBinaryRelation.valueOf(ruleElements[3]);
        goalState = Integer.valueOf(ruleElements[4]);

        if (type == RuleType.passive) {
            timeStart = 0;
            timeEnd   = Parameters.getHorizon() - 1;
            predicate = RuleTimePredicate.conjunction;
        }
        else {
            timePrefix = RuleTimePrefix.valueOf(ruleElements[5]);
            // Link Scheduling Time
            switch (timePrefix) {
                case before:
                    timeStart = 0;
                    timeEnd = Integer.valueOf(ruleElements[6]);
                    predicate = RuleTimePredicate.disjunction;
                    break;
                case after:
                    timeStart = Integer.valueOf(ruleElements[6]);
                    timeEnd = Parameters.getHorizon() - 1;
                    predicate = RuleTimePredicate.disjunction;
                    break;
                case at:
                    timeStart = timeEnd = Integer.valueOf(ruleElements[6]);
                    predicate = RuleTimePredicate.conjunction;
                    break;
                case within:
                    timeStart = Integer.valueOf(ruleElements[6]);
                    timeEnd = Integer.valueOf(ruleElements[7]);
                    predicate = RuleTimePredicate.conjunction;
                    break;
            }
        }

        active = true;

        // Link Predictive model
        predictiveModel = PredictiveModel.valueOf(agtName, location, property);
        // set this predictive model active:
        predictiveModel.setActive(true);
    }

    public RuleType getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public SensorProperty getProperty() {
        return property;
    }

    public RuleBinaryRelation getRelation() {
        return relation;
    }

    public int getGoalState() {
        return goalState;
    }

    public int getTimeStart() {
        return timeStart;
    }

    public int getTimeEnd() {
        return timeEnd;
    }

    public RuleTimePredicate getPredicate() {
        return predicate;
    }

    public PredictiveModel getPredictiveModel() {
        return predictiveModel;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "SchedulingRule{" +
                "type=" + type +
                ", location='" + location + '\'' +
                ", property=" + property +
                ", relation=" + relation +
                ", goalState=" + goalState +
                ", timePrefix=" + timePrefix +
                ", timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", predicate=" + predicate +
                "\n\t predictiveModel=" + predictiveModel +
                '}';
    }


};