package tillerino.tillerinobot.data.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import tillerino.tillerinobot.data.GivenRecommendation;

public interface GivenRecommendationRepository extends
		JpaRepository<GivenRecommendation, Long> {
	List<GivenRecommendation> findByUseridAndDateGreaterThanAndForgottenFalseOrderByDateDesc(
			int userid, long date);

	List<GivenRecommendation> findByUseridAndHiddenFalseOrderByDateDesc(
			int userid);

	@Modifying
	@Query("update givenrecommendations r set r.forgotten = true where r.userid = ?1")
	int forgetAll(int userid);

	@Modifying
	@Query("update givenrecommendations r set r.hidden = true where r.userid = ?1 and r.beatmapid = ?2 and r.mods = ?3")
	int hideRecommendations(int userId, int beatmapid, long mods);
}
