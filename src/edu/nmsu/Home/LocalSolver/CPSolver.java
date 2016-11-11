package edu.nmsu.Home.LocalSolver;


import edu.nmsu.problem.Parameters;
import org.jacop.constraints.XltC;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.core.Var;
import org.jacop.search.*;

/**
 * Created by nandofioretto on 11/1/16.
 */
public abstract class CPSolver implements Solver {

    /** Contains all decision variables used within a specific instance. */
    protected IntVar[] vars;

    /** It specifies the cost function, null if no cost is used. */
    protected IntVar costFunction;

    protected int bestCost = Integer.MAX_VALUE;

    /** It specifies the constraint store. */
    public Store store;

    /** It specifies the search procedure used by a given instance. */
    protected Search<IntVar> search;

    protected long timeoutMs = 300000;

    /** The solving time horizon */
    protected final int HORIZON = Parameters.getHorizon(); // 15 minute intervals over 24 hours

    /** It specifies the scale factors to round double numbers to integers. */
    protected final int kiloWattToWatt = 10;   // used for power
    protected final int centsToDollars = 10;   // used for price
    protected final int deltaScale     = 10;   // used for deltas
    protected  double scaleFactor = kiloWattToWatt*centsToDollars;

    protected int scaleAndRoundPower(double n) {
        return (int)(n * kiloWattToWatt);
    }

    protected int scaleAndRoundPrice(double n) {
        return (int)(n * centsToDollars);
    }

    protected int scaleAndRoundDelta(double n) {
        return (int)(n * deltaScale);
    }

    protected int getKiloWattToWatt() {
        return kiloWattToWatt;
    }

    protected int getCentsToDollars() {
        return centsToDollars;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * It creates an array of int variables
     * @param array The array of variables
     * @param store The constraint store
     * @param min   min value of the domain
     * @param max   max value of the domain
     */
    protected void createIntVarArray(IntVar[] array, Store store, int min, int max) {
        for (int i = 0; i < array.length; i++)
            array[i] = new IntVar(store, min, max);
    }

    /**
     * It creates an array of int variables
     * @param array The array of variables
     * @param store The constraint store
     * @param name  The variable name prefix (will be followed by "_i" for i=0...array.length-1
     * @param min   min value of the domain
     * @param max   max value of the domain
     */
    protected void createIntVarArray(IntVar[] array, Store store, String name, int min, int max) {
        for (int i = 0; i < array.length; i++)
            array[i] = new IntVar(store, name + "_[" + i + "]", min, max);
    }

    /**
     * It creates a 2D array of int variables
     * @param array The array of variables
     * @param store The constraint store
     * @param min   min value of the domain
     * @param max   max value of the domain
     */
    protected void createIntVar2DArray(IntVar[][] array, Store store, int min, int max) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = new IntVar(store, min, max);
            }
        }
    }

    /**
     * It creates a 2D array of int variables
     * @param array The array of variables
     * @param store The constraint store
     * @param name  The variable name prefix (will be followed by "_i" for i=0...array.length-1
     * @param min   min value of the domain
     * @param max   max value of the domain
     */
    protected void createIntVar2DArray(IntVar[][] array, Store store, String name, int min, int max) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = new IntVar(store, name + "_[" + i + "," + j + "]", min, max);
            }
        }
    }

    /**
     * It specifies simple search method based on input order and lexigraphical
     * ordering of values. It optimizes the solution by minimizing the cost function.
     *
     * @return true if there is a solution, false otherwise.
     */
    protected boolean searchOptimal() {
        long T1, T2;
        T1 = System.currentTimeMillis();

        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, null, new IndomainMin<>());
        search.setTimeOut(timeoutMs / 1000);

        boolean result = search.labeling(store, select, costFunction);
        T2 = System.currentTimeMillis();

        System.out.println("\n\t*** Execution time = " + (T2 - T1) + " ms");

        return result;
    }

    protected boolean searchSatisfaction() {

        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, null, new IndomainMax<>());
        search = new DepthFirstSearch<>();
        search.setTimeOut(timeoutMs / 1000);
        boolean result = search.labeling(store, select);
        return result;
    }

    /**
     * It specifies simple search method based on smallest domain variable order
     * and lexigraphical ordering of values.
     * @return true if there is a solution, false otherwise.
     */
    protected boolean searchSmallestDomain() {
        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, new SmallestDomain<>(), new IndomainMin<>());
        search = new DepthFirstSearch<>();
        search.setTimeOut(timeoutMs / 1000);

        boolean result = search.labeling(store, select, costFunction);
        return result;
    }

    /**
     * It specifies simple search method based on weighted degree variable order
     * and lexigraphical ordering of values. This search method is rather general
     * any problem good fit. It can be a good first trial to see if the model is
     * correct.
     *
     * @return true if there is a solution, false otherwise.
     */
    protected boolean searchWeightedDegree() {
        long T1, T2;
        T1 = System.currentTimeMillis();

        SelectChoicePoint<IntVar> select = new SimpleSelect<>(vars,
                new WeightedDegree<>(),
                new SmallestDomain<>(),
                new IndomainMin<>());

        search = new DepthFirstSearch<>();
        search.setTimeOut(timeoutMs / 1000);

        boolean result = search.labeling(store, select, costFunction);

        T2 = System.currentTimeMillis();

        System.out.println("\n\t*** Execution time = " + (T2 - T1) + " ms");
        return result;
    }

    /**
     * It specifies simple search method based variable order which
     * takes into account the number of constraints attached to a variable
     * and lexicographical ordering of values.
     *
     * @return true if there is a solution, false otherwise.
     */
    protected boolean searchMostConstrainedStatic() {
        search = new DepthFirstSearch<>();
        search.setTimeOut(timeoutMs / 1000);

        SelectChoicePoint<IntVar> select = new SimpleSelect<>(vars,
                new MostConstrainedStatic<>(), new IndomainMin<>());

        boolean result = search.labeling(store, select, costFunction);

        return result;
    }

    /**
     * It searches for solution using Limited Discrepancy Search.
     * @param noDiscrepancy maximal number of discrepancies
     * @return true if the solution was found, false otherwise.
     */
    protected boolean searchLDS(int noDiscrepancy) {

        search = new DepthFirstSearch<>();
        search.setTimeOut(timeoutMs / 1000);
        boolean result; // false by default
        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, new SmallestDomain<>(), new IndomainMiddle<>());
        LDS<IntVar> lds = new LDS<>(noDiscrepancy);

        if (search.getExitChildListener() == null)
            search.setExitChildListener(lds);
        else
            search.getExitChildListener().setChildrenListeners(lds);

        // Execution time measurement
        long begin = System.currentTimeMillis();
        result = search.labeling(store, select, costFunction);
        // Execution time measurement
        long end = System.currentTimeMillis();
        System.out.println("Number of milliseconds " + (end - begin));
        return result;
    }

    /**
     * It conducts the search with restarts from which the no-goods are derived.
     * Every search contributes with new no-goods which are kept so eventually
     * the search is complete (although can be very expensive to maintain explicitly
     * all no-goods found during search).
     * @return true if there is a solution, false otherwise.
     */
    protected boolean searchWithRestarts(int nodesOut) {
        // Input Order tie breaking
        boolean result = false;
        boolean timeout = true;

        int nodes = 0;
        int decisions = 0;
        int backtracks = 0;
        int wrongDecisions = 0;

        search = new DepthFirstSearch<>();

        //TODO: fix superfast timeout from this search type. (we want a better solution if we can manage it)
        //look into possibly using this: (was already here commented when I started refactoring stuff)
        //uncommenting it does nothing.. will have to try something else.
        //search.setTimeOut(LPP.getSchedulerTimeoutMs() / 1000);

        NoGoodsCollector<IntVar> collector = new NoGoodsCollector<>();
        search.setExitChildListener(collector);
        search.setTimeOutListener(collector);
        search.setExitListener(collector);

        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, new SmallestDomain<>(), new IndomainSimpleRandom<>());
        //System.out.println("***********************************\n\n\nLPP.getSchedulerTimeoutMs():"+LPP.getSchedulerTimeoutMs()+"\n\n\n***********************");

        //it appears that the timeout is being set to the "NoGoodsCollecter<IntVar> collector" variable.
        //gets set to false if collector timeOut is hit..? kinda strange, will have to investigate further
        //ALWAYS gets set to false...
        while (timeout) {
            // search.setPrintInfo(false);
            search.setNodesOut(nodesOut);

            result = search.labeling(store, select, costFunction);
            timeout &= collector.timeOut;

            nodes += search.getNodes();
            decisions += search.getDecisions();
            wrongDecisions += search.getWrongDecisions();
            backtracks += search.getBacktracks();

            search = new DepthFirstSearch<>();
            collector = new NoGoodsCollector<>();
            search.setExitChildListener(collector);
            search.setTimeOutListener(collector);
            search.setExitListener(collector);
            //System.out.println("++++++" + timeout);
        }
        return result;
    }

    protected boolean BranchAndBound() {
        search = new DepthFirstSearch<>();
        search.setSolutionListener(new CostListener<>());
        store.setLevel(store.level + 1);

        //search.setTimeOut(LPP.getSchedulerTimeoutMs() / 1000);
        SelectChoicePoint<IntVar> select =
                new SimpleSelect<>(vars, new SmallestDomain<>(), new IndomainSimpleRandom<>());

        //search.labeling(store, select, costFunction);
        boolean result = true; boolean optimalResult = false;
        while (result) {
            result = search.labeling(store, select);
            store.impose(new XltC(costFunction, bestCost));
            optimalResult = optimalResult || result;
        }
        store.removeLevel(store.level);
        store.setLevel(store.level-1);
        return result;
    }

    public class CostListener<T extends Var> extends SimpleSolutionListener<T> {
        public boolean executeAfterSolution(Search<T> search,
                                            SelectChoicePoint<T> select) {
            boolean returnCode = super.executeAfterSolution(search, select);
            bestCost = costFunction.value();
            return returnCode;
        }
    }

}
