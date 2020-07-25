package org.tillerino.ppaddict.web.data.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tillerino.ppaddict.web.data.PpaddictUser;

public interface PpaddictUserRepository extends JpaRepository<PpaddictUser, String> {

}
