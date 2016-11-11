package edu.nmsu.Home.LocalSolver;

import edu.nmsu.Home.Devices.Action;
import edu.nmsu.Home.Devices.Actuator;
import edu.nmsu.Home.Devices.Sensor;
import edu.nmsu.Home.Devices.SensorProperty;
import edu.nmsu.Home.Rules.PredictiveModel;
import edu.nmsu.Home.Rules.RuleTimePredicate;
import edu.nmsu.Home.Rules.SchedulingRule;
import edu.nmsu.problem.Pair;
import edu.nmsu.problem.Parameters;
import edu.nmsu.problem.Utilities;
import org.jacop.constraints.*;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by nandofioretto on 11/1/16.
 */
public class RuleSchedulerSolver extends CPSolver {

    // Class Parameters
//    private ArrayList<SchedulingRule> passiveRules;
//    private ArrayList<SchedulingRule> activeRules;
    private ArrayList<SchedulingRule> rules;

    private Map<PredictiveModel, Integer> cpModelId;
    private Map<Integer, PredictiveModel> modelMap;

    private Map<Sensor, Integer> cpSensorId;
    private Map<Integer, Sensor> sensorsMap;

    private Map<Actuator, Integer> cpActuatorId;
    private Map<Integer, Actuator> actuatorsMap;

    // the home agent running this solver
    private String agtName;

    /**
     * var_predModel[s][t] represents the model \Phi_s (the 2nd dimension represents time).
     *      it associates the change in time on the property sensed by s, given the state of
     *      all possible actuators influencing s
     */
    private IntVar[][] var_predModel;

    /**
     * It represents the aggregated power consumption per time step:
     *      var_aggrPower[t] is the aggregated power consumed by all devices at time step t.
     */
    private IntVar[] var_aggrPower;

    /**
     * It represents the vector of decision variables:
     *      x[a][t] represents the decision (action) for actuator a at time t.
     */
    private IntVar[][] x;

    /**
     * delta[s][aID][debug] gives the quantitative effect on the sensor property s
     *                  caused by the device aID on action debug.
     */
    private int[][][] delta;

    /**
     * power[aID][debug]    gives the power (in Watts) consumed by actuator a on action debug
     */
    private int[][] power;

    private double[] bgLoadsKWh;
    private double[] powerPriceKWh = Parameters.getPriceSchema();

    private Pair<Integer, Integer> aggregatedPowerBounds = new Pair<>(0,0);
    private Pair<Integer, Integer> aggregatedPriceBounds = new Pair<>(0,0);

    // weights objective functions
    int alphaPrice = 1;
    int alphaPower = 1;

//    private int MIN_AGGREGATED_POWER  = -1;
//    private int MAX_AGGREGATED_POWER  = -1;
//    private int MAX_AGGREGATED_PRICE  = -1;


    private DebugSchedule debug = new DebugSchedule(); //<---  for debugging
    private final String separatorString = "\n=====================================\n";

    /**
     * Initializes the solver populating the scheduling rules, the predictive model used by it, and the
     * set of sensors and actuators involved.
     * @param schedulingRules Array of rules for the scheduler
     * @param bgLoadsKWh      Array of background load's power consumptions
     */
    public RuleSchedulerSolver(String homeAgtName, ArrayList<SchedulingRule> schedulingRules,  double[] bgLoadsKWh) {

        this.agtName      = homeAgtName;
        this.rules        = new ArrayList<>();
        this.cpModelId    = new HashMap<>();
        this.modelMap     = new HashMap<>();

        this.cpActuatorId = new HashMap<>();
        this.actuatorsMap = new HashMap<>();

        this.cpSensorId   = new HashMap<>();
        this.sensorsMap   = new HashMap<>();

        if (bgLoadsKWh != null)
            this.bgLoadsKWh   = bgLoadsKWh;
        else
            this.bgLoadsKWh = new double[HORIZON];

        if (debug.intro())
            System.out.println( separatorString + "TEST start: Intro" + separatorString);


        // Initializes all (activated) Scheduling Rules
        rules.addAll(schedulingRules.stream().filter(r -> r.isActive()).collect(Collectors.toList()));

        if (debug.intro()) {
            for (SchedulingRule rule : rules) {
                System.out.println(rule.toString());
            }
        }

        // Initialize Models ID to each model (avoid duplicate models)
        // to the variables "property" in the CP model
        int mid = 0;
        for (SchedulingRule r : rules) {
            if (!cpModelId.containsKey(r.getPredictiveModel())) {
                if (debug.intro()) {
                    System.out.println("Adding predicting model " + mid + ": " + r.getPredictiveModel().toString());
                }
                cpModelId.put(r.getPredictiveModel(), mid);
                modelMap.put(mid, r.getPredictiveModel());
                mid++;
            }
        }

        int aid = 0, sid = 0;
        Set<Actuator> actuatorSet = new HashSet<>();

        // Populate set of sensors and actuators
        for (SchedulingRule r : rules) {
            Sensor sensor = r.getPredictiveModel().getSensor();
            if (!sensorsMap.containsValue(sensor)) {
                cpSensorId.put(sensor, sid);
                sensorsMap.put(sid, sensor);
                sid++;
            }
            actuatorSet.addAll(r.getPredictiveModel().getActuators().stream().collect(Collectors.toList()));
        }

        for (Actuator actuator : actuatorSet) {
            if (!actuatorsMap.containsValue(actuator)) {
                cpActuatorId.put(actuator, aid);
                actuatorsMap.put(aid, actuator);
                aid++;
            }
        }

        if (debug.intro()) {
            System.out.println("List of all actuators and sensors in the CP model");
            for (Actuator a : actuatorsMap.values()) {
                System.out.println("actuator " + a.getName());
            }

            for (Sensor s : sensorsMap.values()) {
                System.out.println("sensor " + s.getName());
            }
        }


        ////////////////////////////////
        // Domain Bounds
        ////////////////////////////////

        int aggrMax = 0;
        for (Actuator actuator : actuatorsMap.values()) {
            int maxPower = 0;
            for (Action a : actuator.getActions()) {
                maxPower = Math.max(maxPower, scaleAndRoundPower(a.getPowerKWh()));
            }
            aggrMax += maxPower;
        }
        aggregatedPowerBounds.setSecond(aggrMax);

        int mp = 0;
        for (double p : Parameters.getPriceSchema()) {
            mp = Math.max(mp, scaleAndRoundPrice(p));
        }
        aggregatedPriceBounds.setSecond(mp * aggrMax);

        if (debug.intro()) {
            System.out.println("Aggr Power bounds = " + aggregatedPowerBounds.toString() +
                    " Aggr Price Bounds= " + aggregatedPriceBounds.toString());
            System.out.println(separatorString);
        }

        ////////////////////////////////
        // Model Declarations
        ////////////////////////////////
        // the number of active actuators.
        int nbActuators = actuatorsMap.size();
        // The properties associated to the rules
        int nbPredictiveModels = cpModelId.size();
        // The maximal number of actions for any actuator device in the pool of active actuators
        int nbActions    = 0;
        for (Actuator actuator : actuatorsMap.values()) {
            nbActions = Math.max(nbActions, actuator.getNbActions());
        }
        delta  = new int[nbPredictiveModels][nbActuators][nbActions];
        populateDeltaArray(delta);
        power  = new int[nbActuators][nbActions];
        populatePowerArray(power);
    }

    public void setWeights(int w_price, int w_power) {
        alphaPrice = w_price;
        alphaPower = w_power;
    }

    @Override
    public void model(double[] neighborPower) {
        // the number of active actuators.
        final int NUM_ACTUATORS = actuatorsMap.size();
        // The properties associated to the rules
        final int PREDMODEL_SIZE = cpModelId.size();

        int maxNeibhgoursPwConsumption = scaleAndRoundPower(Utilities.getMax(neighborPower));
        int minNeibhgoursPwConsumption = scaleAndRoundPower(Utilities.getMin(neighborPower));
        int minBgLoad = scaleAndRoundPower(Utilities.getMin(bgLoadsKWh));
        int maxBgLoad = scaleAndRoundPower(Utilities.getMax(bgLoadsKWh));

        ////////////////////////////////
        // CP Declarations
        ////////////////////////////////
        store = new Store();
        vars = new IntVar[NUM_ACTUATORS * HORIZON];


        ////////////////////////////////
        // Variables
        ////////////////////////////////
        var_predModel = new IntVar[PREDMODEL_SIZE][HORIZON];
        createIntVar2DArray(var_predModel, store, "predModel",
                IntDomain.MinInt,
                IntDomain.MaxInt);

        var_aggrPower = new IntVar[HORIZON];
        createIntVarArray(var_aggrPower, store, "aggrPower",
                aggregatedPowerBounds.getFirst() + minBgLoad,
                aggregatedPowerBounds.getSecond() + maxBgLoad);

        x   = new IntVar[NUM_ACTUATORS][HORIZON];
        // Populate the array of int_variables x with domains = {0,...n_actions_x - 1}
        for (int aID = 0; aID < NUM_ACTUATORS; aID++) {
            Actuator actuator = actuatorsMap.get(aID);
            createIntVarArray(x[aID], store, "x_"+ actuator.getName(), 0, actuator.getNbActions() - 1);
        }

        //The dollar price of the final solution:
        IntVar objPrice = new IntVar(store, "price",
                aggregatedPriceBounds.getFirst(),
                aggregatedPriceBounds.getSecond());

        //The power difference objective:
        // min = (0) all off + min_neighb^2
        // max = ((maxAggrPw all on at t) + (max_neighb_pw))^2
        IntVar objPowerDiff = new IntVar(store, "pwDiff", //0, IntDomain.MaxInt);
                (int) Math.pow(aggregatedPowerBounds.getFirst()  + minNeibhgoursPwConsumption, 2) * HORIZON,
                (int) Math.pow(aggregatedPowerBounds.getSecond() + maxNeibhgoursPwConsumption, 2) * HORIZON);

        ////////////////////////////////////////////
        // Constraints:
        /////////////////////////////////////////////
        // Predictive model constraints
        for (PredictiveModel model : cpModelId.keySet()) {
            int mID = cpModelId.get(model);
            createPredictiveModelConstraints(model, var_predModel[mID], delta[mID], x);
        }

        // Constraints: Active Rules
        for (SchedulingRule rule : rules) {
            PredictiveModel model = PredictiveModel.valueOf(agtName, rule.getLocation(), rule.getProperty());
            int mID = cpModelId.get(model);

            createRuleConstraints(rule, var_predModel[mID]);
        }

        // Constraint: Aggregate Power reduction
        if (debug.power())
            System.out.println( separatorString + "TEST start: Power " + separatorString);

        for (int t = 0; t < HORIZON; t++) {
            // exclusively this agent power!!!
            createAggrPowerConstraint(var_aggrPower[t], x, t, power, bgLoadsKWh[t]);
        }

        if (debug.power())
            System.out.println(separatorString);


        ////////////////////////////////////////////
        // Objectives
        ////////////////////////////////////////////
        // 1. minimize  \sum_t1 \sum_t2  |power[t1] - power[t2]|
        // createObjectivePower(objPowerDiff, var_aggrPower, neighborPower);
        createObjectivePeaks(objPowerDiff, var_aggrPower, neighborPower);

        // 2. Total power price
        createObjectivePrice(objPrice, var_aggrPower, powerPriceKWh);


        // normalize:
//        System.out.println("max power: " + objPowerDiff.max() + "  max price: " + objPrice.max());
//        if (objPowerDiff.max() > objPrice.max()) {
//            alphaPrice = Math.max(1, objPowerDiff.max()/objPrice.max());
//        } else {
//            alphaPower = Math.max(1, objPrice.max()/objPowerDiff.max());
//        }
        if (debug.objPower()) {
            System.out.println("========================\nobjPowerDiff: " + objPowerDiff.max()
                    + "\nobjPrice: " + objPrice.max() + "\n=======================");
        }

        int maxDom = (alphaPower * objPowerDiff.max() + alphaPrice * objPrice.max());
        costFunction = new IntVar(store, "costFunction", 0, maxDom);
        Constraint ctr = new LinearInt(store,
                new IntVar[]{objPowerDiff,objPrice,costFunction},
                new int[]{alphaPower,alphaPrice,-1}, "==", 0);
        store.impose(ctr);
//        costFunction = objPrice;

        ////////////////////////////////////////////
        // Search
        ////////////////////////////////////////////
        // Unroll decision variables into array vars:
        for (int aID = 0; aID < NUM_ACTUATORS; aID++) {
            System.arraycopy(x[aID], 0, vars, aID * HORIZON, HORIZON);
        }
    }


    @Override
    public RulesSchedule getSchedule(double[] neighborPower) {
        model(neighborPower);

        //super.searchOptimal();
        //super.searchWithRestarts(1000);
        //super.BranchAndBound();

        if (super.searchMostConstrainedStatic()) {
//            if (debug.schedule())
//                printPredictiveModels();
            return constructSchedule();
        } else {  //if search fails, create a generic RulesSchedule object and set cost to max size of double.
            RulesSchedule rulesSchedule = new RulesSchedule();
            System.err.println("Error: Schedule not found!");

            /*
            int nbActuators = actuatorsMap.size();
            for (int aID = 0; aID < nbActuators; aID++) {
                Device actuator = actuatorsMap.get(aID);
                ActuatorSchedule aSchedule = new ActuatorSchedule(actuator);
            }
            */

            rulesSchedule.setCost(Double.MAX_VALUE);
            return rulesSchedule;
        }
    }

    //Does the same thing as getSchedule(),
    //but with no neighbor power and using searchWithRestarts(1000) instead of searchMostConstrainedStatic()
    @Override
    public RulesSchedule getFirstSchedule() {
        double neighborPower[] = new double[HORIZON];
        for (int i = 0; i < HORIZON; i++) neighborPower[i] = 0;
        model(neighborPower);

        if (super.searchSatisfaction()) { //if (super.searchWithRestarts(1000)) {
            if (debug.schedule())
                printPredictiveModels();
            return constructSchedule();
        } else {  //if search fails, create a generic RulesSchedule object and set cost to max size of double.
            System.out.println("Error: First Schedule not found!");
            RulesSchedule rulesSchedule = new RulesSchedule();

            /*
            int nbActuators = actuatorsMap.size();
            for (int aID = 0; aID < nbActuators; aID++) {
                Device actuator = actuatorsMap.get(aID);
                ActuatorSchedule aSchedule = new ActuatorSchedule(actuator);
            }
            */

            rulesSchedule.setCost(Double.MAX_VALUE);
            return rulesSchedule;
        }
    }

    //same as getSchedule but with searchSmallestDomain()
    @Override
    public RulesSchedule getBaselineSchedule(double[] neighborPower) {
        model(neighborPower);

        if (super.searchSatisfaction()) {
            if (debug.schedule())
                printPredictiveModels();
            return constructSchedule();
        } else { //if search fails, create a generic RulesSchedule object and set cost to max size of double.
            System.out.println("Error: Baseline Schedule not found!");
            RulesSchedule rulesSchedule = new RulesSchedule();

            /*
            int nbActuators = actuatorsMap.size();
            for (int aID = 0; aID < nbActuators; aID++) {
                Device actuator = actuatorsMap.get(aID);
                ActuatorSchedule aSchedule = new ActuatorSchedule(actuator);
            }
            */

            rulesSchedule.setCost(Double.MAX_VALUE);
            return rulesSchedule;
        }
    }

    @Override
    public boolean check() {
        return false;
    }


    private RulesSchedule constructSchedule() {
        RulesSchedule rulesSchedule = new RulesSchedule();

        int nbActuators = actuatorsMap.size();
        // Unroll decision variables into array vars:
        for (int aID = 0; aID < nbActuators; aID++) {
            Actuator actuator = actuatorsMap.get(aID);
            ActuatorSchedule aSchedule = new ActuatorSchedule(actuator);

            // t is an index for timesteps, x[aID][t] is the index of the actuator's action
            for (int t = 0; t < HORIZON; t++) {
                aSchedule.setAction(t, x[aID][t].value());
            }
            // store schedule for device aID
            rulesSchedule.insert(aSchedule);
        }

        // Store power result in the rulesScheduler.
        for (int t = 0; t < HORIZON; t++) {
            double power = var_aggrPower[t].value() / (double)getKiloWattToWatt();
            rulesSchedule.setPowerConsumptionKw(t, power);
        }
        // Set cost of this schedule
        rulesSchedule.setCost((double)costFunction.value()/scaleFactor);

        return rulesSchedule;
    }

    private void printPredictiveModels() {
        // Print predictive model:
        for (PredictiveModel model : cpModelId.keySet()) {
            int mID = cpModelId.get(model);
            System.out.print(model.toRead() + "\t:");
            for (int t = 0; t < HORIZON; t++) {
                double value = var_predModel[mID][t].value() / (double)deltaScale;
                if(value <  10.0) System.out.print(" ");
                if(value < 100.0) System.out.print(" ");
                System.out.print(value + " ");
                //while(ret.length() < 5) ret += " ";
            }
            System.out.println("");
        }

    }

    /**
     * Populate the array delta:
     *      delta[s][aID][debug] gives the quantitative effect on the sensor property s (MODEL)
     *                       caused by the device aID on action debug.
     *
     * NOTE: each delta value is scaled by 'deltaScale'
     * @param delta Array giving the change per unit time in property s cause by device aID given action debug.
     */
    private void populateDeltaArray(int[][][] delta) {

        if (debug.delta())
            System.out.println( separatorString + "TEST start: delta array" + separatorString);

        int nbProperties = delta.length;

        for (int mID = 0; mID < nbProperties; mID++) {
            PredictiveModel model = modelMap.get(mID);
            SensorProperty sensorProperty = model.getProperty();

            if(debug.delta())
                System.out.println("sensor property of the model: " + sensorProperty.toString());

            for (Actuator actuator : model.getActuators()) {
                if (debug.delta())
                    System.out.println(actuator.getName());

                int aID = cpActuatorId.get(actuator);
                int sID = 0;

                for (Action action : actuator.getActions()) {
                    // set delta of sensor_property or 0 (if not found)
                    delta[mID][aID][sID] = scaleAndRoundDelta(action.getDeltaOf(sensorProperty));

                    if(debug.delta()) {
                        String name = "Delta["+mID+"]["+aID+"]["+sID+"](" + action.getName() +")";
                        System.out.println(name + " = " + delta[mID][aID][sID]);
                    }
                    sID++;
                }
            }
        }
        if (debug.delta())
            System.out.println(separatorString);
    }

    /**
     * It populate the array power, used in the constraint solver.
     * @param power      power[aID][debug] gives the power (in Watts) consumed by actuator a on action debug
     */
    private void populatePowerArray(int[][] power) {

        if (debug.power())
            System.out.println( separatorString + "TEST start: power array" + separatorString);

        int nbActuators = power.length;
        for (int aID = 0; aID < nbActuators; aID++) {
            Actuator actuator = actuatorsMap.get(aID);

            int sID = 0;
            for (Action action : actuator.getActions()) {
                power[aID][sID] = scaleAndRoundPower(action.getPowerKWh());   // transforms KW to Watt

                if(debug.power())
                    System.out.println("actuator " + actuator.getName() + ": action = "
                            + action.getName() +  " pw: " +  power[aID][sID]);
                sID ++;
            }
        }
        if (debug.power())
            System.out.println(separatorString);

    }

    /**
     * It creates (and impose on the store) the constraints related to the predictive model.
     * For each predictive model s, it relates the effect of the devices that may affect s, during
     * the whole time HORIZON. The general equation is:
     *      model[s][t] =  model[s][t-1]  +  \Sum_{a in As} delta[s][a][t-1]
     *      where:
     *          As is the set of actuators influencing the sensing property s in the model.
     *
     * NOTE: Each value of the predictive model 'model[s][t]' is scaled by a factor of deltaScale
     * @param model The predictive model
     * @param x the set of decision variables (one per device, and per time-step)
     * @param var_PredModel_m the variables associated to the predictive model m (one per time step)
     * @param delta_m     the delta values associated to each device which affects the model m
     */
    private void createPredictiveModelConstraints(PredictiveModel model,
                                                  IntVar[] var_PredModel_m,
                                                  int[][] delta_m,
                                                  IntVar[][] x) {
        if (debug.predictiveModel())
            System.out.println( separatorString + "TEST start: Predictive model: " + model.toString() + separatorString);

        int cpInitialState = scaleAndRoundDelta(model.getSensor().getCurrentState());

        Constraint ctr0 = new XeqC(var_PredModel_m[0], cpInitialState);
        store.impose( ctr0 );
        if (debug.predictiveModel()) {
            System.out.println("constraint for time 0: " + ctr0.toString());
        }

        for (int t = 1; t < HORIZON; t++) {
            // Aux vars to compute sum of  property[[t] + \Sum_a delta_a[action_a]
            ArrayList<IntVar> auxVars = new ArrayList<>();

            // CONSTRAINT SUM
            for (Actuator actuator : model.getActuators()) {
                int aID = cpActuatorId.get(actuator);
                String name = "aux_delta_"+actuator.getName()+"(t="+t+")";
                IntVar vAux = new IntVar(store, name, Utilities.getMin(delta_m[aID]), Utilities.getMax(delta_m[aID]));

                // c = delta[aID][ x[aID][t] ];
                // todo: check if t or t-1 -> when action propagates
                int xDom = actuator.getNbActions();
                int[] delta = Arrays.copyOf(delta_m[aID], xDom);

                Constraint ctr = new Element(x[aID][t], delta, vAux, -1);
                store.impose(ctr);    // index, array[index],

                if (debug.predictiveModel())
                    System.out.println("imposed element constraint " + ctr.toString() );

                auxVars.add(vAux);
            }
            auxVars.add(var_PredModel_m[t-1]);// assign previous element to aux_vars[T]

            // Sum variables to create: var_predMoldel_m[t+1] = var_predMoldel_m[t] + \Sum_a delta_a[action_a]
            Constraint ctr = new SumInt(store, auxVars, "==", var_PredModel_m[t]);
            store.impose( ctr );   // sum

            if (debug.predictiveModel())
                System.out.println("imposed sum constraint " + ctr.toString() );
        }

        if (debug.predictiveModel())
            System.out.println(separatorString);
    }


    /**
     * It creates the constraints associated to a rule. In particular, for each rule:
     *      <locaion> <property[mID]> <relation> <goal_state> <time[t]>
     *      it constraints the var_PredModel[mID][t] related to the model mID to be equal to <goal_state>
     *      at time t.
     *      Predicates, before, after, within, are handled as disjunctions or conjucntions of constraints.
     *
     * NOTE: Each rule's goal state is scaled by a factor given by deltaFactor
     * @param rule  The rule associated predictive model mID (and to the constraint)
     * @param var_predModel_r the variable associated to the predictive model of this rule
     */
    private void createRuleConstraints(SchedulingRule rule, IntVar[] var_predModel_r) {

        if (debug.rules())
            System.out.println( separatorString + "TEST start: Rule: " + rule.toString() + separatorString);

        ArrayList<PrimitiveConstraint> c = new ArrayList<>();

        // TODO: Check Time start, time end (closed or opened set)
        // Todo: Check - rule can never start from time 0
        for (int t = rule.getTimeStart() ; t < rule.getTimeEnd(); t++) {
            int cpGoalState = scaleAndRoundDelta(rule.getGoalState());
            switch (rule.getRelation()) {
                case eq:
                    c.add(new XeqC(var_predModel_r[t], cpGoalState));
                    break;
                case neq:
                    c.add(new XneqC(var_predModel_r[t], cpGoalState));
                    break;
                case lt:
                    c.add(new XltC(var_predModel_r[t], cpGoalState));
                    break;
                case gt:
                    c.add(new XgtC(var_predModel_r[t], cpGoalState));
                    break;
                case leq:
                    c.add(new XlteqC(var_predModel_r[t], cpGoalState));
                    break;
                case geq:
                    c.add(new XgteqC(var_predModel_r[t], cpGoalState));
                    break;
            }
        }
        if (rule.getPredicate() == RuleTimePredicate.conjunction) {
            Constraint ctr = new And(c);
            if (debug.rules())
                System.out.println("constraint : " + ctr.toString());
            store.impose(ctr);
        }
        else {
            Constraint ctr = new Or(c);
            if (debug.rules())
                System.out.println("constraint : " + ctr.toString());
            store.impose(ctr);
        }

        if (debug.rules())
            System.out.println(separatorString);

    }

    /**
     * It creates the constraints which aggregates the power consumption of each device used at
     * time step t
     * @param var_aggrPower_t   the variables associated to the aggregated power consumption at time t
     * @param x                 the decision variables
     * @param t                 the time step
     * @param power             the values for the power
     */
    private void createAggrPowerConstraint(IntVar var_aggrPower_t, IntVar[][] x, int t,
                                           int[][] power, double bgLoadsKW) {

        // Contains the variables related to the power consumed in the current time step.
        // Used to sum among all of them
        ArrayList<IntVar> auxVars = new ArrayList<>();
        int nbActuators = power.length;

        for (int aID = 0; aID < nbActuators; aID++) {
            IntVar vAux = new IntVar(store, Utilities.getMin(power[aID]), Utilities.getMax(power[aID]));

            int xDom = actuatorsMap.get(aID).getNbActions();
            int[] power_a = Arrays.copyOf(power[aID], xDom);

            Constraint ctr = new Element(x[aID][t], power_a, vAux, -1);
            store.impose(ctr);

            if (debug.power()) {
                System.out.println("element constraint: " + ctr.toString());
            }
            auxVars.add(vAux);
        }
        // It adds the background loads
        IntVar vAuxBg = new IntVar(store, scaleAndRoundPower(bgLoadsKW), scaleAndRoundPower(bgLoadsKW));
        auxVars.add(vAuxBg);

        // It sums the total power at time step t
        Constraint ctr = new SumInt(store, auxVars, "==", var_aggrPower_t);
        store.impose(ctr);

        if (debug.power())
            System.out.println("sum constraint: " + ctr.toString());
    }


    /**
     *  minimize    \sum_t1 \sum_t2  |power[t1] - power[t2]|
     * @param objPowerDiff  Sum of differences in power consumptions between all time steps.
     * @param var_aggrPower Array of power consumed for each time step.
     * @param neighborhsKW  Array of neighbor's loads for each time step.
     */
    private void createObjectivePower(IntVar objPowerDiff, IntVar[] var_aggrPower, double[] neighborhsKW) {

        if(debug.objPower())
            System.out.println(separatorString + "TEST Start Obj: Power " + separatorString);

        int minNeighb = scaleAndRoundPower(Utilities.getMin(neighborhsKW));
        int maxNeighb = scaleAndRoundPower(Utilities.getMax(neighborhsKW));

        ArrayList<IntVar> auxVars = new ArrayList<>();
        IntVar[] auxVarsPower = new IntVar[HORIZON];
        for (int t = 0; t < HORIZON; t++) {
            auxVarsPower[t] = new IntVar(store,
                    aggregatedPowerBounds.getFirst() + minNeighb,
                    aggregatedPowerBounds.getSecond() + maxNeighb);
            Constraint ctr = new XplusCeqZ(var_aggrPower[t], scaleAndRoundPower(neighborhsKW[t]), auxVarsPower[t]);
            store.impose(ctr);
        }

        for (int t1 = 0; t1 < HORIZON; t1++) {
            for (int t2 = t1+1; t2 < HORIZON; t2++) {
                IntVar vAux = new IntVar(store,
                        aggregatedPowerBounds.getFirst() + minNeighb,
                        aggregatedPowerBounds.getSecond() + maxNeighb);
                Constraint ctr = new Distance(auxVarsPower[t1], auxVarsPower[t2], vAux);
                store.impose(ctr);
                auxVars.add(vAux);

                if (debug.objPower())
                    System.out.println("Diff constraint [" + t1 + ", " + t2 + "]:" + ctr.toString());
            }
        }
        Constraint ctrPower = new SumInt(store, auxVars, "==", objPowerDiff);
        store.impose(ctrPower);


        if (debug.objPower())
            System.out.println(separatorString);
    }


    /**
     *  minimize price
     * @param objPrice      Sum for all time steps of the product of power consumed multiplied by price per time step.
     * @param var_aggrPower Array showing power consumed in all time steps.
     * @param powerPriceKWh Array showing the price of power in all time steps.
     */
    private  void createObjectivePrice(IntVar objPrice, IntVar[] var_aggrPower, double[] powerPriceKWh) {
        if (debug.price())
            System.out.println(separatorString + "TEST Start Obj: Price " + separatorString);

        int[] scaledPrice = new int[HORIZON];
        for (int t=0; t<HORIZON; t++)
            scaledPrice[t] = scaleAndRoundPrice(powerPriceKWh[t]);

        // Sums the current aggregated power per time step (in Watts) * the price (in KwH)
        Constraint ctrPrice = new SumWeight(var_aggrPower, scaledPrice, objPrice);
        store.impose(ctrPrice);

        if (debug.price())
            System.out.println("weighted sum: " + ctrPrice.toString());

        if (debug.price())
            System.out.println(separatorString);

    }


    /**
     * Peak minimization function:
     * It creates the following function:
     *
     * \sum_t  ( \sum_{a \in devices}  powerKWh[a][t])^2
     *
     * This is equivalent to one which minimize the energy peaks.
     *
     */
    private void createObjectivePeaks(IntVar objPowerPeak, IntVar[] var_aggrPower, double[] neighborhsKW) {

        if(debug.objPower())
            System.out.println(separatorString + "TEST Start Obj: Power " + separatorString);

        IntVar pow2 = new IntVar(store, 2, 2);  // used to compute squares

        ArrayList<IntVar> auxVars = new ArrayList<>();  // stores vars to sum (external summation)
        IntVar[] auxVarsPower = new IntVar[HORIZON];    // stores aggregated power of this agent + neighbor agents per time step

        for (int t = 0; t < HORIZON; t++) {
            int aggrPowerUB = aggregatedPowerBounds.getSecond() + scaleAndRoundPower(neighborhsKW[t]);
            int aggrPowerLB = aggregatedPowerBounds.getFirst() + scaleAndRoundPower(neighborhsKW[t]);

            // curr agent power consumed at time t + neighbors' agent power consumed at time t
            auxVarsPower[t] = new IntVar(store, aggrPowerLB, aggrPowerUB);
            store.impose ( new XplusCeqZ(var_aggrPower[t], scaleAndRoundPower(neighborhsKW[t]), auxVarsPower[t]) );

            // squares auxVarsPower[t]
            IntVar vAux = new IntVar(store, aggrPowerLB*aggrPowerLB, (aggrPowerUB)*(aggrPowerUB));

            store.impose( new XexpYeqZ(auxVarsPower[t], pow2, vAux) ) ;

                //store.impose( new XmulCeqZ(auxVarsPower[t], 2, vAux) ) ;
            //store.impose(new XeqY(auxVarsPower[t], vAux));
            auxVars.add(vAux);

            if(debug.objPower())
                System.out.println(vAux);

        }

        // Sum the squares of auxVarsPower[t]
        Constraint ctrPower = new SumInt(store, auxVars, "==", objPowerPeak);
        store.impose(ctrPower);

        if (debug.objPower())
            System.out.println(separatorString);

    }

}
