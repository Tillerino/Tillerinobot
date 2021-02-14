package tillerino.tillerinobot.data.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import tillerino.tillerinobot.data.BotConfig;

public interface BotConfigRepository extends JpaRepository<BotConfig, String> {

}
