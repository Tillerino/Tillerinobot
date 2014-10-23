package tillerino.tillerinobot.mbeans;

import com.google.common.cache.Cache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CacheMXBeanImpl extends AbstractMBeanRegistration implements CacheMXBean {
 
	private final Cache<?, ?> cache;
 
	@SuppressFBWarnings(value = "NP", justification = "false positive")
	public <K, V> CacheMXBeanImpl(Cache<K, V> cache) {
		this(cache, null, null);
    }
	
	public <K, V> CacheMXBeanImpl(Cache<K, V> cache, Class<?> packageClass, String name) {
		super(packageClass, CacheMXBean.class, name);
		this.cache = cache;
	}

	@Override
	public long getSize() {
		return cache.size();
	}

	@Override
	public void cleanUp() {
		cache.cleanUp();
	}

	@Override
	public void invalidateAll() {
		cache.invalidateAll();
	}

	@Override
    public long getRequestCount() {
        return cache.stats().requestCount();
    }
 
    @Override
    public long getHitCount() {
        return cache.stats().hitCount();
    }
 
    @Override
    public double getHitRate() {
        return cache.stats().hitRate();
    }
 
    @Override
    public long getMissCount() {
        return cache.stats().missCount();
    }
 
    @Override
    public double getMissRate() {
        return cache.stats().missRate();
    }
 
    @Override
    public long getLoadCount() {
        return cache.stats().loadCount();
    }
 
    @Override
    public long getLoadSuccessCount() {
        return cache.stats().loadSuccessCount();
    }
 
    @Override
    public long getLoadExceptionCount() {
        return cache.stats().loadExceptionCount();
    }
 
    @Override
    public double getLoadExceptionRate() {
        return cache.stats().loadExceptionRate();
    }
 
    @Override
    public long getTotalLoadTime() {
        return cache.stats().totalLoadTime();
    }
 
    @Override
    public double getAverageLoadPenalty() {
        return cache.stats().averageLoadPenalty();
    }
 
    @Override
    public long getEvictionCount() {
        return cache.stats().evictionCount();
    }
}