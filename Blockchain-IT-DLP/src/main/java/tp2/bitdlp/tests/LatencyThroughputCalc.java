package tp2.bitdlp.tests;

public class LatencyThroughputCalc
{
    private long lMax, lMin;

    private long totalTime;
    private int nOps;
    
    /**
     * 
     */
    public LatencyThroughputCalc() {
        lMax = -1;
        lMin = Long.MAX_VALUE;
        totalTime = 0;
        nOps = 0;
    }

    /**
     * @return the lMax
     */
    public long getlMax() {
        return lMax;
    }

    /**
     * @return the lMin
     */
    public long getlMin() {
        return lMin;
    }

    /**
     * @return the totalTime
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * @return the nOps
     */
    public int getnOps() {
        return nOps;
    }

    public double calcThroughput()
    {
        return (double)nOps / ((double)totalTime / 1000);
    }

    public long calcAvgLatency()
    {
        return totalTime / nOps;
    }

    public void addLatency(long l)
    {
        totalTime += l;
        nOps++;
        lMax = Long.max(lMax, l);
        lMin = Long.min(lMin, l);
    }

    
    @Override
    public String toString() {
        return "Latency (ms): Avg=" + calcAvgLatency() + ", Max=" + lMax + ", Min=" + lMin + "\nThroughput (op/s): "
            + calcThroughput() + ", nOps=" + nOps
                + ", totalTime(s)=" + totalTime / 1000 + "\n\n";
    }

    
}
