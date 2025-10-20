package org.tillerino.ppaddict.web.data;

import javax.annotation.CheckForNull;
import org.tillerino.osuApiModel.types.UserId;

public interface HasLinkedOsuId {
    @CheckForNull
    @UserId
    Integer getLinkedOsuId();

    void setLinkedOsuId(@UserId Integer osuId);
}
