package org.tillerino.ppaddict.web.data;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JdbcUpdate;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.ppaddict.web.types.PpaddictId;

@Table(name = "ppaddictusers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PpaddictUser {
    @PpaddictId
    @Id
    private String identifier;

    @CheckForNull
    private String data;

    @PpaddictId
    private String forward;

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcInsert
        void insert(Connection c, PpaddictUser u) throws SQLException;

        @JdbcUpdate("replace into ppaddictusers (u.#columns) values (:u.#values)")
        void replace(Connection c, PpaddictUser u) throws SQLException;

        @JdbcSelect(where = "`identifier` = :identifier")
        Optional<PpaddictUser> findByIdentifier(Connection c, String identifier) throws SQLException;
    }
}
