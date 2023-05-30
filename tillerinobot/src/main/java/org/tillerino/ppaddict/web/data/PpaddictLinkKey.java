package org.tillerino.ppaddict.web.data;

import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.ppaddict.web.types.PpaddictId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("ppaddictlinkkeys")
@KeyColumn("linkKey")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PpaddictLinkKey {
	private @PpaddictId String identifier;

	private String displayName;

	private String linkKey;

	private long expires;
}
