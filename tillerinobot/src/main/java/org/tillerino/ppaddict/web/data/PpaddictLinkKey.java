package org.tillerino.ppaddict.web.data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.ppaddict.web.types.PpaddictId;

@Table(name = "ppaddictlinkkeys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PpaddictLinkKey {
    private @PpaddictId String identifier;

    private String displayName;

    @Id
    private String linkKey;

    private long expires;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert
        void insert(Connection c, PpaddictLinkKey k) throws SQLException;

        @JdbcSelect(where = "`linkKey` = :linkKey")
        Optional<PpaddictLinkKey> findByLinkKey(Connection c, String linkKey) throws SQLException;
    }
}
