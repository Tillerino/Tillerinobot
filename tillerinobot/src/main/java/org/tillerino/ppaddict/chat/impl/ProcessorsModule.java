package org.tillerino.ppaddict.chat.impl;

import org.tillerino.ppaddict.chat.GameChatEventConsumer;
import org.tillerino.ppaddict.chat.GameChatResponseConsumer;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * Binds {@link MessagePreprocessor} and {@link ResponsePostprocessor}.
 */
public class ProcessorsModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(GameChatEventConsumer.class).annotatedWith(Names.named("messagePreprocessor")).to(MessagePreprocessor.class);
		bind(GameChatResponseConsumer.class).annotatedWith(Names.named("responsePostprocessor"))
				.to(ResponsePostprocessor.class);
	}
}
