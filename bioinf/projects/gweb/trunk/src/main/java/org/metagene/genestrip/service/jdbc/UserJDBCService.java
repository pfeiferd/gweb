package org.metagene.genestrip.service.jdbc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.genestrip.service.ServiceException;
import org.metagene.genestrip.service.UserService;
import org.metagene.genestrip.service.dto.User;

public class UserJDBCService extends AbstractDTOJDBCService<User> implements UserService {
	private final MessageDigest digest;

	public UserJDBCService(DataSource dataSource) {
		this(dataSource, true);
	}

	public UserJDBCService(DataSource dataSource, boolean hashPassword) {
		super(dataSource);

		if (hashPassword) {
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}
		} else {
			digest = null;
		}
	}

	protected String hashPassword(String password) {
		if (password == null) {
			return null;
		}
		if (digest == null) {
			return password;
		}
		synchronized (digest) {
			digest.reset();
			digest.update(password.getBytes());
			return new String(digest.digest());			
		}
	}
	
	@Override
	public User login(String login, String password) {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement("select * from sysuser where login = ? and password = ?");
			ps.setString(1, login);
			ps.setString(2, hashPassword(password));
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
		res.setPassword(null);
		res.setAllowAll(rs.getBoolean(4));
		res.setAllowJobs(rs.getBoolean(5));
		res.setPersonId(rs.getLong(6));
		return res;
	}
	
	@Override
	protected int toPreparedStatement(PreparedStatement ps, User d) throws SQLException {
		ps.setString(1, d.getLogin());
		ps.setString(2, hashPassword(d.getPassword()));
		ps.setBoolean(3, d.isAllowAll());
		ps.setBoolean(4, d.isAllowJobs());
		ps.setLong(5, d.getPersonId());
		return 5;
	}
	
	@Override
	public void logout() {
	}

	@Override
	public long create(User d) {
		return create(d, "insert into sysuser(login, password, allow_all, allow_jobs, person_id) values (?,?,?,?,?)");
	}

	@Override
	public boolean remove(long id) {
		return remove(id, "delete from sysuser where id = ?");
	}

	@Override
	public User get(long id) {
		return get(id, "select * from sysuser where id = ?");
	}

	@Override
	public List<User> getAll() {
		return getAll("select * from sysuser");
	}

	@Override
	public boolean update(User d) {
		return update(d, "update sysuser set login = ?, password = ?, allow_all = ?, allow_jobs = ?, person_id = ? where id = ?");
	}
}
