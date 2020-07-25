package org.tillerino.ppaddict.web;

import javax.annotation.CheckForNull;

import org.tillerino.osuApiModel.types.UserId;
import org.tillerino.ppaddict.web.data.HasLinkedOsuId;

import lombok.Data;

@Data
public class ExamplePpaddictUserData implements HasLinkedOsuId {
	@CheckForNull
	private String data;

	@CheckForNull
	private @UserId Integer linkedOsuId;
}
