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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.gweb.service.CannotRemoveException;
import org.metagene.gweb.service.NotFoundException;
import org.metagene.gweb.service.ServiceException;
import org.metagene.gweb.service.ValidationException;
import org.metagene.gweb.service.dto.DTO;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

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

	protected int toPreparedUpdateStatement(PreparedStatement ps, T d) throws SQLException {
		return toPreparedStatement(ps, d);
	}

	protected int toPreparedCreateStatement(PreparedStatement ps, T d) throws SQLException {
		return toPreparedStatement(ps, d);
	}

	protected long create(T d, String sql) {
		validate(d);

		Connection c = null;
		try {
			c = getConnection();

			try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
				toPreparedCreateStatement(ps, d);
				ps.executeUpdate();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					while (rs.next()) {
						d.setId(rs.getLong(1));
					}
				}
			}
			return d.getId();
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new ValidationException(e);
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected void update(T d, String sql) {
		validate(d);

		Connection c = null;
		try {
			c = getConnection();

			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setLong(toPreparedUpdateStatement(ps, d) + 1, d.getId());
				if (ps.executeUpdate() == 0) {
					throw new NotFoundException("DTO with id not found.");
				}
			}
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new ValidationException(e);
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected void remove(long id, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setLong(1, id);
				if (ps.executeUpdate() == 0) {
					throw new NotFoundException("DTO with id not found.");
				}
			}
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new CannotRemoveException(e);
		} catch (SQLException e) {
			handleSystemSpecificConstraintViolation(e);
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}
	
	protected void handleSystemSpecificConstraintViolation(SQLException e) { 
		if (e instanceof PSQLException) {
			if (PSQLState.FOREIGN_KEY_VIOLATION.getState().equals(((PSQLException) e).getSQLState())) {
				throw new CannotRemoveException(e); 
			}
		}
	}

	protected T get(long id, String sql) {
		return get(id, Types.BIGINT, sql);
	}

	protected T get(Object selector, int sqlType, String sql) {
		Connection c = null;
		try {
			c = getConnection();

			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setObject(1, selector, sqlType);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						return fromResultSet(rs);
					}
				}
			}
			return null;
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new ValidationException(e);
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

			try (PreparedStatement ps = c.prepareStatement(sql)) {
				if (filler != null) {
					filler.fill(ps);
				}
				List<T> l = new ArrayList<T>();
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						l.add(fromResultSet(rs));
					}
				}
				return l;
			}
		} catch (SQLIntegrityConstraintViolationException e) {
			throw new ValidationException(e);
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected List<T> getAll(String sql) {
		return getBySelection(sql, null);
	}

	protected long[] toArray(List<Long> values) {
		long[] result = new long[values.size()];
		int i = 0;
		for (Long l : values) {
			result[i++] = l == null ? DTO.INVALID_ID : l;
		}
		return result;
	}

	protected interface PSFiller {
		public void fill(PreparedStatement ps) throws SQLException;
	}

	protected void validate(T d) {
		if (!d.checkValid()) {
			throw new ValidationException("Invalid DTO");
		}
	}
}
