package tillerino.tillerinobot.data.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import tillerino.tillerinobot.data.UserNameMapping;

public interface UserNameMappingRepository extends
		JpaRepository<UserNameMapping, String> {

}
