package org.tillerino.ppaddict.web.data;

import javax.annotation.CheckForNull;

import org.tillerino.mormon.KeyColumn;
import org.tillerino.mormon.Table;
import org.tillerino.ppaddict.web.types.PpaddictId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("ppaddictusers")
@KeyColumn("identifier")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PpaddictUser {
	@PpaddictId
	private String identifier;

	@CheckForNull
	private String data;

	@PpaddictId
	private String forward;
}
