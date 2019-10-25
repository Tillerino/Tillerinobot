package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.chat.GameChatResponse.none;

import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;

public class GameChatResponseTest {
	static class Aresponse implements GameChatResponse {
		@Override
		public Iterator<GameChatResponse> iterator() {
			return Collections.singletonList((GameChatResponse) this).iterator();
		}
	}

	@Test
	public void noneAndThenSomething() throws Exception {
		Aresponse something = new Aresponse();
		assertThat(none().then(something).iterator()).contains(something);
	}

	@Test
	public void somethingAndThenNone() throws Exception {
		Aresponse something = new Aresponse();
		assertThat(something.then(none()).iterator()).contains(something);
	}

	@Test
	public void somethingAndThenNull() throws Exception {
		Aresponse something = new Aresponse();
		assertThat(something.then(null).iterator()).contains(something);
	}

	@Test
	public void somethingAndThenSomethingElse() throws Exception {
		Aresponse something = new Aresponse();
		Aresponse somethingElse = new Aresponse();
		assertThat(something.then(somethingElse).iterator()).containsExactly(something, somethingElse);
	}

	@Test
	public void somethingAndThenList() throws Exception {
		Aresponse something = new Aresponse();
		Aresponse somethingElse = new Aresponse();
		Aresponse evenMore = new Aresponse();
		assertThat(something.then(somethingElse.then(evenMore)).iterator()).containsExactly(something, somethingElse, evenMore);
	}

	@Test
	public void listAndThenSomething() throws Exception {
		Aresponse something = new Aresponse();
		Aresponse somethingElse = new Aresponse();
		Aresponse evenMore = new Aresponse();
		assertThat(something.then(somethingElse).then(evenMore).iterator()).containsExactly(something, somethingElse, evenMore);
	}

	@Test
	public void noneIsNoneAndNotNoneIsNotNone() throws Exception {
		assertThat(none().isNone()).isTrue();
		assertThat(new Aresponse().isNone()).isFalse();
	}

	@Test
	public void noneHasEmptyIterator() throws Exception {
		assertThat(none().iterator()).isEmpty();
	}

	@Test
	public void noneToString() throws Exception {
		assertThat(none().toString()).isEqualTo("[No Response]");
	}
}
