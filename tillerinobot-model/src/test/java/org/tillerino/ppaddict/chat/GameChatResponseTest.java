package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.chat.GameChatResponse.none;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class GameChatResponseTest {
    static class Aresponse implements GameChatResponse {
        @Override
        public Iterable<GameChatResponse> flatten() {
            return Collections.singletonList(this);
        }
    }

    @Test
    public void noneAndThenSomething() {
        Aresponse something = new Aresponse();
        assertThat(none().then(something).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenNone() {
        Aresponse something = new Aresponse();
        assertThat(something.then(none()).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenNull() {
        Aresponse something = new Aresponse();
        assertThat(something.then(null).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenSomethingElse() {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        assertThat(something.then(somethingElse).flatten()).containsExactly(something, somethingElse);
    }

    @Test
    public void somethingAndThenList() {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        Aresponse evenMore = new Aresponse();
        assertThat(something.then(somethingElse.then(evenMore)).flatten())
                .containsExactly(something, somethingElse, evenMore);
    }

    @Test
    public void listAndThenSomething() {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        Aresponse evenMore = new Aresponse();
        assertThat(something.then(somethingElse).then(evenMore).flatten())
                .containsExactly(something, somethingElse, evenMore);
    }

    @Test
    public void noneIsNoneAndNotNoneIsNotNone() {
        assertThat(none().isNone()).isTrue();
        assertThat(new Aresponse().isNone()).isFalse();
    }

    @Test
    public void noneHasEmptyIterator() {
        assertThat(none().flatten()).isEmpty();
    }

    @Test
    public void noneToString() {
        assertThat(none().toString()).isEqualTo("[No Response]");
    }
}
