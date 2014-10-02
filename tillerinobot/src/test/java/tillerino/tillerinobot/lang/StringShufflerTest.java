package tillerino.tillerinobot.lang;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;


public class StringShufflerTest {
	@Test
	public void testShuffling() {
		Random rnd = new Random();
		
		StringShuffler shuffler = new StringShuffler(rnd);
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		for(int i = 1; i <= 100; i++) {
			String[] strings = { "a", "b", "c", "d", "e" };
			
			for (int j = 0; j < strings.length; j++) {
				String s = shuffler.get(strings);
				
				Integer x = map.get(s);
				if(x == null) {
					x = 0;
				}
				
				x++;
				
				map.put(s, x);
			}
			
			for (Integer count : map.values()) {
				assertEquals(i, (int) count);
			}
		}
	}
}
