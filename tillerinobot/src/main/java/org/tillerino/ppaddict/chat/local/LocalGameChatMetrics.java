package org.tillerino.ppaddict.chat.local;

import javax.inject.Singleton;

import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Singleton
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LocalGameChatMetrics extends GameChatClientMetrics {
	private long responseQueueSize;
	private long eventQueueSize;

	@org.mapstruct.Mapper
	public interface Mapper {
		static final Mapper INSTANCE = Mappers.getMapper(Mapper.class);

		void loadFromBot(GameChatClientMetrics source, @MappingTarget GameChatClientMetrics target);

		LocalGameChatMetrics copy(LocalGameChatMetrics l);
	}
}