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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.metagene.gweb.service.ServiceException;
import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.dto.User.UserRole;

public class UserJDBCService extends AbstractDTOJDBCService<User> implements UserService {
	private final MessageDigest digest;

	public UserJDBCService(DataSource dataSource) {
		super(dataSource);

		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	protected byte[] hashPassword(String password) {
		synchronized (digest) {
			digest.reset();
			digest.update(password.getBytes());
			return digest.digest();
		}
	}

	@Override
	public User login(String login, String password) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement("select * from sysuser where role > "
					+ UserRole.NO_LOGIN.ordinal() + " and login = ? and password = ?");
			ps.setString(1, login);
			ps.setBytes(2, hashPassword(password));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return fromResultSet(rs);
			}
			return null;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	@Override
	protected User fromResultSet(ResultSet rs) throws SQLException {
		User res = new User();
		res.setId(rs.getLong(1));
		res.setLogin(rs.getString(2));
		res.setRole(UserRole.indexToValue(rs.getInt(3)));
		res.setPersonId(rs.getLong(4));
		res.setPassword(null);
		return res;
	}

	@Override
	protected int toPreparedStatement(PreparedStatement ps, User d) throws SQLException {
		if (d.getPassword() == null) {
			ps.setString(1, d.getLogin());
			ps.setInt(2, d.getRole().ordinal());
			ps.setLong(3, d.getPersonId());
			return 3;
		} else {
			ps.setString(1, d.getLogin());
			ps.setBytes(2, hashPassword(d.getPassword()));
			ps.setInt(3, d.getRole().ordinal());
			ps.setLong(4, d.getPersonId());
			return 4;
		}
	}

	@Override
	public void logout() {
	}

	@Override
	public long create(User d) {
		return create(d, "insert into sysuser(login, password, role, person_id) values (?,?,?,?)");
	}

	@Override
	public void remove(long id) {
		remove(id, "delete from sysuser where id = ?");
	}

	@Override
	public User get(long id) {
		return get(id, "select * from sysuser where id = ?");
	}

	@Override
	public User getByLogin(String login) {
		return get(login, Types.VARCHAR, "select * from sysuser where login = ?");
	}

	@Override
	public User[] getAll() {
		return getAll("select * from sysuser order by id").toArray(new User[0]);
	}

	@Override
	public void update(User d) {
		if (d.getPassword() == null) {
			update(d, "update sysuser set login = ?, role = ?, person_id = ? where id = ?");
		} else {
			update(d, "update sysuser set login = ?, password = ?, role = ?, person_id = ? where id = ?");
		}
	}

	@Override
	public User getLoggedInUser() {
		throw new UnsupportedOperationException();
	}
}
