package edu.nmsu.problem;

/**
 * Created by nandofioretto on 11/2/16.
 */
public final class Parameters {

    private static int horizon = 12;
    private static String deviceDictionaryPath = "resources/DeviceDictionary.json";
    private static double[] priceSchema =
        {0.198, 0.198, 0.198, 0.198, 0.225, 0.225, 0.249, 0.849, 0.849, 0.225, 0.225, 0.198};
    private static long schedulerTimeoutMs = 10000;
    private static int mgmNbCycles = 50;

    public static int getHorizon() {
        return horizon;
    }

    public static long getSchedulerTimeoutMs() {
        return schedulerTimeoutMs;
    }

    public static double[] getPriceSchema() {
        return priceSchema;
    }

    public static String getDeviceDictionaryPath() {
        return deviceDictionaryPath;
    }

    public static int getMGMnbCycles() { return mgmNbCycles; }

}
