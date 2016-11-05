package edu.nmsu.Home.LocalSolver;

/**
 * Created by nandofioretto on 11/1/16.
 */
public interface Solver {

    /**
     * Get a valid schedule if the solver terminates within timeout.
     * Otherwise it returns a schedule with null actions and high cost.
     * @param neighborPower
     * @return
     */
    RulesSchedule getSchedule(double[] neighborPower);

    /**
     * Ignores timeout and returns a schedule
     */
    RulesSchedule getFirstSchedule();

    /**
     * Function used for baseline comparison.
     * @return
     */
    RulesSchedule getBaselineSchedule(double[] neighborPower);

    /**
     * It checks if the current instance is satisfied, given the problem constraints.
     * @return
     */
    boolean check();

    /**
     * It constructs a model for the problem instance.
     *
     * @param neighborPower  The neighbors power consumption per timeStep
     */
    public void model(double[] neighborPower);


}
