package tillerino.tillerinobot.data.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import tillerino.tillerinobot.data.BotUserData;

public interface BotUserDataRepository extends JpaRepository<BotUserData, Integer> {
	BotUserData findByUserId(int userId);
}
