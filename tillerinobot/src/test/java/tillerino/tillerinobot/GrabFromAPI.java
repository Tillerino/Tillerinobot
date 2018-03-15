package tillerino.tillerinobot;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

public class GrabFromAPI {
	public static void main(String[] args) throws Exception {
		System.out.printf("MIN MAX AVG MED%n");
		URL url = new URL("http://192.168.178.99:8080/keys/16208da0f32b1dc5c9ded276fd186f57");
		for (int j = 0; j < 1000; j++) {
			List<Integer> times = new ArrayList<>();
			for (int i = 0; i < 101; i++) {
				long time = System.nanoTime();
				try (InputStream is = url.openStream();) {
					IOUtils.copy(is, new NullOutputStream());
				}
				times.add((int) (System.nanoTime() - time));
			}
			Supplier<DoubleStream> supplier = () -> times.stream().mapToDouble(x -> x / 1E6);
			Collections.sort(times);
			double median = times.get(50) / 1E6;
			System.out.printf("%.2f %.2f %.2f %.2f%n", supplier.get().min().getAsDouble(), supplier.get().max().getAsDouble(), supplier.get().average().getAsDouble(), median);
		}
	}
}
