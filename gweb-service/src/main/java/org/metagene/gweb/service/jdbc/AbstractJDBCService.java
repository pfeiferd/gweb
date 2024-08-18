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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import org.metagene.gweb.service.ServiceException;

public abstract class AbstractJDBCService {
	private final ThreadLocal<Connection> fixedConnection;

	private final DataSource dataSource;

	public AbstractJDBCService(DataSource dataSource) {
		this.dataSource = dataSource;
		fixedConnection = new ThreadLocal<Connection>();
	}

	private void fixConnection() throws SQLException {
		if (fixedConnection.get() == null) {
			fixedConnection.set(getConnection());
		}
		else {
			throw new ServiceException(new IllegalStateException("Connection already fixed"));
		}
	}

	private Connection unfixConnection() {
		Connection c = fixedConnection.get();
		if (c != null) {
			fixedConnection.set(null);
		}
		else {
			throw new ServiceException(new IllegalStateException("Connection already unfixed"));			
		}
		
		return c;
	}

	protected void startTX() {
		try {
			fixConnection();
			getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			try {
				throw new ServiceException(e);
			} finally {
				releaseConnection(unfixConnection());
			}
		}
	}

	protected void endTX(boolean commit) {
		try {
			Connection c = getConnection();
			if (commit) {
				c.commit();
			}
			else {
				c.rollback();
			}
			c.setAutoCommit(true);
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(unfixConnection());
		}
	}

	protected Connection getConnection() throws SQLException {
		Connection c = fixedConnection.get();
		if (c != null) {
			return c;
		}
		return dataSource.getConnection();
	}

	protected void releaseConnection(Connection c) {
		if (c != null && c != fixedConnection.get()) {
			try {
				if (!c.getAutoCommit()) {
					c.setAutoCommit(true);
				}
				c.close();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}
		}
	}

	protected Timestamp convertTimestamp(Date date) {
		return date == null ? null : new Timestamp(date.getTime());
	}

	protected Date convertTimestamp(Timestamp date) {
		return date == null ? null : new Date(date.getTime());
	}
}
