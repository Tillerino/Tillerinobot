package org.tillerino.ppaddict.chat.local;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.tillerino.ppaddict.chat.GameChatClientMetrics;

@Singleton
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(onConstructor_ = @Inject)
public class LocalGameChatMetrics extends GameChatClientMetrics {
    private long lastSentMessage;
    private long lastRecommendation;
    private long responseQueueSize;
    private long eventQueueSize;

    @org.mapstruct.Mapper
    public interface Mapper {
        Mapper INSTANCE = Mappers.getMapper(Mapper.class);

        void loadFromBot(GameChatClientMetrics source, @MappingTarget GameChatClientMetrics target);

        LocalGameChatMetrics copy(LocalGameChatMetrics l);
    }
}
