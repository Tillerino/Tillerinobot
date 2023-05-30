/**
 * mORMon = minimal ORM Mysql-ONly.
 *
 * <p>
 * A tiny ORM that only takes care of the basics:
 * mapping between Java objects and {@link ResultSet}/{@link PreparedStatement}
 * with some convenience stuff like streaming and batching sprinkled on top.
 *
 * <p>
 * The "where" part of queries is written in plain SQL.
 * Everything is built for MySQL (e.g. how to escape column names, streaming, and batching).
 *
 * <p>
 * Start by creating a {@link Database} instance.
 * For convenience, {@link DatabaseManager} implements a pool for {@link Database} instances.
 */
package org.tillerino.mormon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
