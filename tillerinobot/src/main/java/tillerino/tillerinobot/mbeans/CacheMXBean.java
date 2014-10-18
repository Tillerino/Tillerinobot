package tillerino.tillerinobot.mbeans;

public interface CacheMXBean {
	public long getSize();

	public void cleanUp();

	public void invalidateAll();

    public long getRequestCount();
 
    public long getHitCount();
 
    public double getHitRate();
 
    public long getMissCount();
 
    public double getMissRate();
 
    public long getLoadCount();
 
    public long getLoadSuccessCount();
 
    public long getLoadExceptionCount();
 
    public double getLoadExceptionRate();
 
    public long getTotalLoadTime();
 
    public double getAverageLoadPenalty();
 
    public long getEvictionCount();
}