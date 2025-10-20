package org.tillerino.ppaddict.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tillerino.ppaddict.chat.GameChatResponse.none;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class GameChatResponseTest {
    static class Aresponse implements GameChatResponse {
        @Override
        public Iterable<GameChatResponse> flatten() {
            return Collections.singletonList((GameChatResponse) this);
        }
    }

    @Test
    public void noneAndThenSomething() throws Exception {
        Aresponse something = new Aresponse();
        assertThat(none().then(something).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenNone() throws Exception {
        Aresponse something = new Aresponse();
        assertThat(something.then(none()).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenNull() throws Exception {
        Aresponse something = new Aresponse();
        assertThat(something.then(null).flatten()).contains(something);
    }

    @Test
    public void somethingAndThenSomethingElse() throws Exception {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        assertThat(something.then(somethingElse).flatten()).containsExactly(something, somethingElse);
    }

    @Test
    public void somethingAndThenList() throws Exception {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        Aresponse evenMore = new Aresponse();
        assertThat(something.then(somethingElse.then(evenMore)).flatten())
                .containsExactly(something, somethingElse, evenMore);
    }

    @Test
    public void listAndThenSomething() throws Exception {
        Aresponse something = new Aresponse();
        Aresponse somethingElse = new Aresponse();
        Aresponse evenMore = new Aresponse();
        assertThat(something.then(somethingElse).then(evenMore).flatten())
                .containsExactly(something, somethingElse, evenMore);
    }

    @Test
    public void noneIsNoneAndNotNoneIsNotNone() throws Exception {
        assertThat(none().isNone()).isTrue();
        assertThat(new Aresponse().isNone()).isFalse();
    }

    @Test
    public void noneHasEmptyIterator() throws Exception {
        assertThat(none().flatten()).isEmpty();
    }

    @Test
    public void noneToString() throws Exception {
        assertThat(none().toString()).isEqualTo("[No Response]");
    }
}
