package org.tillerino.ppaddict.web.data;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.tillerino.ppaddict.web.types.PpaddictId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "ppaddictlinkkeys")
@NoArgsConstructor
@AllArgsConstructor
public class PpaddictLinkKey {
	private @PpaddictId String identifier;

	private String displayName;

	@Id
	private String linkKey;

	private long expires;
}
