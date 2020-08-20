package org.tillerino.ppaddict.web.data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.tillerino.ppaddict.web.types.PpaddictId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "ppaddictlinkkeys")
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
