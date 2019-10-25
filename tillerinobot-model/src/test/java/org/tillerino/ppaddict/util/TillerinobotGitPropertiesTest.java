package org.tillerino.ppaddict.util;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Named;

import org.junit.Test;

public class TillerinobotGitPropertiesTest {
	@Test
	public void nameValueIsCopiedToConstructor() throws Exception {
		// Checks if Lombok is amazing
		assertThat(TillerinobotGitProperties.class.getDeclaredConstructors()[0].getParameters()[0].getAnnotation(Named.class))
			.satisfies(annotation -> assertThat(annotation.value()).isEqualTo("tillerinobot.git.commit.id.abbrev"));
	}
}
