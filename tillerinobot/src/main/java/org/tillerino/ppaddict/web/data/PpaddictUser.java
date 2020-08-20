package org.tillerino.ppaddict.web.data;

import javax.annotation.CheckForNull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.tillerino.ppaddict.web.types.PpaddictId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ppaddictusers")
@Entity(name = "ppaddictusers")
public class PpaddictUser {
	@PpaddictId
	@Id
	private String identifier;

	@CheckForNull
	@Column(length = 1024 * 1024)
	private String data;

	@PpaddictId
	private String forward;
}
