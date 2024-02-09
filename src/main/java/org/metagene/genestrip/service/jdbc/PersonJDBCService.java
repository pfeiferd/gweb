package org.metagene.genestrip.service.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.genestrip.service.PersonService;
import org.metagene.genestrip.service.dto.Person;

public class PersonJDBCService extends AbstractDTOJDBCService<Person> implements PersonService {
	public PersonJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public long create(Person d) {
		return create(d, "insert into person(first_name, last_name) values (?,?)");
	}

	@Override
	protected Person fromResultSet(ResultSet rs) throws SQLException {
		Person res = new Person();
		res.setId(rs.getLong(1));
		res.setFirstName(rs.getString(2));
		res.setLastName(rs.getString(3));
		
		return res;
	}
	
	@Override
	protected int toPreparedStatement(PreparedStatement ps, Person d) throws SQLException {
		ps.setString(1, d.getFirstName());
		ps.setString(2, d.getLastName());
		return 2;
	}
	
	@Override
	public boolean remove(long id) {
		return remove(id, "delete from person where id = ?");
	}	

	@Override
	public Person get(long id) {
		return get(id, "select * from person where id = ?");
	}

	@Override
	public List<Person> getAll() {
		return getAll("select * from person");
	}

	@Override
	public boolean update(Person d) {
		return update(d, "update person set first_name = ?, last_name = ? where id = ?");
	}
}
