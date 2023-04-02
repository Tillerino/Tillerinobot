package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultTest {
	ObjectMapper jackson = new ObjectMapper();

	@Test
	public void jackson() throws Exception {
		Result<String, Integer> r;
		assertThat(jackson.writeValueAsString(r = Result.ok("hello"))).isEqualTo("{\"ok\":\"hello\"}");
		assertThat(jackson.writeValueAsString(r = Result.err(123))).isEqualTo("{\"err\":123}");

		r = jackson.readValue("{\"ok\":\"hello\"}", new TypeReference<>() {
		});
		assertThat(r).isEqualTo(Result.ok("hello"));

		r = jackson.readValue("{\"err\":123}", new TypeReference<>() {
		});
		assertThat(r).isEqualTo(Result.err(123));
	}
}
