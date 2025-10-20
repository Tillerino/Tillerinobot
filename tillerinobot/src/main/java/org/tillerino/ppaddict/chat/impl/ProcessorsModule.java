package org.tillerino.ppaddict.chat.impl;

import javax.inject.Named;
import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;

/** Binds {@link MessagePreprocessor} and {@link ResponsePostprocessor}. */
@dagger.Module
public interface ProcessorsModule {
    @dagger.Binds
    @Named("messagePreprocessor")
    GameChatEventConsumer provideMessagePreprocessor(MessagePreprocessor impl);

    @dagger.Binds
    @Named("responsePostprocessor")
    GameChatResponseConsumer provideResponsePostprocessor(ResponsePostprocessor impl);
}
