package org.metagene.genestrip.service.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.metagene.genestrip.service.ServiceException;

public abstract class AbstractJDBCService {
	private final DataSource dataSource;
	
	public AbstractJDBCService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	protected Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	protected void releaseConnection(Connection c) {
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}
		}
	}
	
	protected Timestamp convertTimestamp(java.util.Date date) {
		return date == null ? null : new Timestamp(date.getTime());
	}
	
	protected Date convertTimestamp(Timestamp date) {
		return date == null ? null : new Date(date.getTime());
	}
}
