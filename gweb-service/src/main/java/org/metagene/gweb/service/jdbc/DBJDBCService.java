/*
 * 
 * “Commons Clause” License Condition v1.0
 * 
 * The Software is provided to you by the Licensor under the License, 
 * as defined below, subject to the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License 
 * will not include, and the License does not grant to you, the right to Sell the Software.
 * 
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted 
 * to you under the License to provide to third parties, for a fee or other consideration 
 * (including without limitation fees for hosting or consulting/ support services related to 
 * the Software), a product or service whose value derives, entirely or substantially, from the 
 * functionality of the Software. Any license notice or attribution required by the License 
 * must also include this Commons Clause License Condition notice.
 * 
 * Software: gweb
 * 
 * License: Apache 2.0
 * 
 * Licensor: Daniel Pfeifer (daniel.pfeifer@progotec.de)
 * 
 */
package org.metagene.gweb.service.jdbc;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.ValidationException;
import org.metagene.gweb.service.dto.DB;

public class DBJDBCService extends AbstractDTOJDBCService<DB> implements DBService {
	public DBJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long create(DB d) {
		return create(d, "insert into db(name, db_file_prefix, install_url, install_md5) values (?,?,?,?)");
	}

	@Override
	protected DB fromResultSet(ResultSet rs) throws SQLException {
		DB res = new DB();
		res.setId(rs.getLong(1));
		res.setName(rs.getString(2));
		res.setDbFilePrefix(rs.getString(3));
		res.setInstallURL(rs.getString(4));
		res.setInstallMD5(rs.getString(5));
		
		return res;
	}
	
	@Override
	protected int toPreparedStatement(PreparedStatement ps, DB d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setString(2, d.getDbFilePrefix());
		ps.setString(3, d.getInstallURL());
		ps.setString(4, d.getInstallMD5());
		return 4;
	}
	
	@Override
	protected int toPreparedUpdateStatement(PreparedStatement ps, DB d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setString(2, d.getInstallURL());
		ps.setString(3, d.getInstallMD5());
		return 3;
	}
	
	@Override
	public void remove(long id) {
		remove(id, "delete from db where id = ?");
	}	

	@Override
	public DB get(long id) {
		return get(id, "select * from db where id = ?");
	}

	@Override
	public DB[] getAll() {
		return getAll("select * from db order by id").toArray(new DB[0]);
	}

	@Override
	public void update(DB d) {
		DB oldDB = get(d.getId());
		if (!oldDB.getDbFilePrefix().equals(d.getDbFilePrefix())) {
			throw new ValidationException("Cannot update db file prefix for db");
		}
		update(d, "update db set name = ?, install_url = ?, install_md5 = ? where id = ?");
	}
	
	@Override
	public File getInfoFile(long dbId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}
}
