package org.metagene.genestrip.service.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.genestrip.service.ServiceException;
import org.metagene.genestrip.service.dto.DTO;

public abstract class AbstractDTOJDBCService<T extends DTO> extends AbstractJDBCService {
	public AbstractDTOJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	protected T fromResultSet(ResultSet rs) throws SQLException {
		return null;
	}

	protected int toPreparedStatement(PreparedStatement ps, T d) throws SQLException {
		return 0;
	}

	protected long create(T d, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			toPreparedStatement(ps, d);
			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			while (rs.next()) {
				d.setId(rs.getLong(1));
			}
			return d.getId();
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected boolean update(T d, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement(sql);
			ps.setLong(toPreparedStatement(ps, d) + 1, d.getId());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}
	
	protected boolean remove(long id, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement(sql);
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected T get(long id, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement(sql);
			ps.setLong(1, id);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				return fromResultSet(rs);
			}
			return null;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected List<T> getBySelection(String sql, PSFiller filler) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement(sql);
			if (filler != null) {
				filler.fill(ps);
			}
			ResultSet rs = ps.executeQuery();
			List<T> l = new ArrayList<T>();
			while (rs.next()) {
				l.add(fromResultSet(rs));
			}
			return l;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}
	
	protected List<T> getAll(String sql) {
		return getBySelection(sql, null);
	}
	
	public interface PSFiller {
		public void fill(PreparedStatement ps) throws SQLException;
	}
}
