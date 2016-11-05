package edu.nmsu.Home.LocalSolver;

/**
 * Begrudgingly created by William Kluegel on 10/1/2016.
 */
/**
 *  Crappy methods for storing whether to debug each variable...
 *  Being used to remove warnings related to if(true) always true compile warning..
 */

public class DebugSchedule {
    public boolean intro()          { return  true; }
    public boolean schedule()       { return  true; }
    public boolean power()          { return  true; }
    public boolean delta()          { return  true; }

    public boolean predictiveModel(){ return true; }
    public boolean rules()          { return true; }
    public boolean objPower()       { return true; }
    public boolean price()          { return true; }

}
