package tillerino.tillerinobot.recommendations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

import lombok.Getter;

/**
 * Distribution for recommendations. Changes upon sampling.
 * 
 * @author Tillerino
 * @param T domain to sample from
 * @param S settings which were used to fill this sampler
 */
public class Sampler<T, S> {
	private final SortedMap<Double, T> distribution = new TreeMap<>();
	private double sum = 0;
	private final Random random = new Random();
	@Getter
	private final S settings;
	private final ToDoubleFunction<T> probabilityDistribution;

	/**
	 * Creates a new Sampler.
	 *
	 * @param population the entire set of elements to sample from
	 * @param settings the settings that were used to create this distribution (stored for convenience).
	 * @param probabilityDistribution the probablilities for each element. Is normalized automatically, i.e. does not need to sum up to one.
	 */
	public Sampler(Collection<T> population, S settings, ToDoubleFunction<T> probabilityDistribution) {
		for (T elem : population) {
			sum += probabilityDistribution.applyAsDouble(elem);
			distribution.put(sum, elem);
		}
		this.settings = settings;
		this.probabilityDistribution = probabilityDistribution;
	}

	public boolean isEmpty() {
		return distribution.isEmpty();
	}

	public synchronized T sample() {
		double x = random.nextDouble() * sum;
		
		SortedMap<Double, T> rest = distribution.tailMap(x);
		if(rest.size() == 0) {
			// this means that there was some extreme numerical instability.
			// this is practically not possible. at least not *maximum stack
			// size* times in a row.
			return sample();
		}
		
		sum = rest.firstKey();
		
		T sample = rest.remove(sum);
		
		sum -= probabilityDistribution.applyAsDouble(sample);
		
		Collection<T> refill = new ArrayList<>();
		
		while(!rest.isEmpty()) {
			refill.add(rest.remove(rest.firstKey()));
		}
		
		for(T elem : refill) {
			sum += probabilityDistribution.applyAsDouble(elem);
			distribution.put(sum, elem);
		}
		
		return sample;
	}
}