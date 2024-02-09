package org.metagene.genestrip.service.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.DBInfo;

public class DBJDBCService extends AbstractDTOJDBCService<DB> implements DBService {
	public DBJDBCService(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public DBInfo getDBInfo(long dbID) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long create(DB d) {
		return create(d, "insert into db(name, db_file_prefix) values (?,?)");
	}

	@Override
	protected DB fromResultSet(ResultSet rs) throws SQLException {
		DB res = new DB();
		res.setId(rs.getLong(1));
		res.setName(rs.getString(2));
		res.setDbFilePrefix(rs.getString(3));
		
		return res;
	}
	
	@Override
	protected int toPreparedStatement(PreparedStatement ps, DB d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setString(2, d.getDbFilePrefix());
		return 2;
	}
	
	@Override
	public boolean remove(long id) {
		return remove(id, "delete from db where id = ?");
	}	

	@Override
	public DB get(long id) {
		return get(id, "select * from db where id = ?");
	}

	@Override
	public List<DB> getAll() {
		return getAll("select * from db");
	}

	@Override
	public boolean update(DB d) {
		return update(d, "update db set name = ?, db_file_prefix = ? where id = ?");
	}
}
