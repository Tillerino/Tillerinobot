package tillerino.tillerinobot.data;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.tillerino.jagger.annotations.JdbcConfig;
import org.tillerino.jagger.annotations.JdbcInsert;
import org.tillerino.jagger.annotations.JdbcSelect;
import org.tillerino.jagger.annotations.JsonConfig;
import org.tillerino.osuApiModel.types.BeatmapId;

@Table(name = "actualbeatmaps")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressFBWarnings(value = "EI")
public class ActualBeatmap {
    @Id
    @Nonnull
    private Integer beatmapid = 0;

    @CheckForNull
    private byte[] content;

    /**
     * By default, MySQL throws up if you try to store more than 16MiB in one packet. The way we are set up here - no
     * streaming and somehow doubling the packet size through UTF-16 encoding or something - we can't store beatmaps
     * larger than 8MiB in size. We could try to fix that with configuration. The more sustainable option is compressing
     * the beatmaps right away: For the large beatmaps, this is very efficient. E.g. beatmap 2571051 which is 14MiB is
     * compressed down to 700KiB with gzip (no settings).
     */
    @CheckForNull
    private byte[] gzipContent;

    private long downloaded;
    private String hash;

    public byte[] decompressedContent() {
        if (content != null) {
            return content;
        }
        if (gzipContent != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(gzipContent));
                IOUtils.copy(gzip, baos);
            } catch (IOException e) {
                throw new IllegalStateException("Decompression failed", e);
            }
            return baos.toByteArray();
        }
        throw new IllegalStateException("Neither uncompressed nor compressed data is present.");
    }

    public void compressContent(byte[] raw) {
        content = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            IOUtils.copy(new ByteArrayInputStream(raw), gzip);
        } catch (IOException e) {
            throw new IllegalStateException("Decompression failed", e);
        }
        gzipContent = baos.toByteArray();
    }

    @JdbcConfig(quoteChar = "`")
    @JsonConfig(onGeneratedClass = Singleton.class, onGeneratedConstructors = Inject.class)
    public interface Repo {
        @JdbcSelect(where = "`beatmapid` = :beatmapid")
        Optional<ActualBeatmap> findOneById(Connection c, @BeatmapId int beatmapid) throws SQLException;

        @JdbcInsert
        void insert(Connection c, ActualBeatmap entity) throws SQLException;

        @JdbcInsert("REPLACE INTO `actualbeatmaps` (`entity.#columns`) VALUES (:entity.#values)")
        void replace(Connection c, ActualBeatmap entity) throws SQLException;
    }
}
