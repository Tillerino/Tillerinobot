package org.tillerino.ppaddict.util;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Data
public class TillerinobotGitProperties {
	@Named("tillerinobot.git.commit.id.abbrev")
	private final String commit;
	@Named("tillerinobot.git.commit.message.short")
	private final String commitMessage;
}