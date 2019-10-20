package org.tillerino.ppaddict.util;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;

@Data
public class TillerinobotGitProperties {
	private final String commit;
	private final String commitMessage;

	@Inject
	public TillerinobotGitProperties(@Named("tillerinobot.git.commit.id.abbrev") String commit,
			@Named("tillerinobot.git.commit.message.short") String commitMessage) {
		this.commit = commit;
		this.commitMessage = commitMessage;
	}
}