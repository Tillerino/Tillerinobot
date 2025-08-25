package org.tillerino.ppaddict.chat.impl;

import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;

import javax.inject.Named;

/**
 * Binds {@link MessagePreprocessor} and {@link ResponsePostprocessor}.
 */
@dagger.Module
public interface ProcessorsModule {
	@dagger.Binds
	@Named("messagePreprocessor")
	GameChatEventConsumer provideMessagePreprocessor(MessagePreprocessor impl);

  @dagger.Binds
	@Named("responsePostprocessor")
	GameChatResponseConsumer provideResponsePostprocessor(ResponsePostprocessor impl);
}
