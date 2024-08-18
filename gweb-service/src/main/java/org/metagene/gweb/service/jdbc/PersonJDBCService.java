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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.metagene.gweb.service.PersonService;
import org.metagene.gweb.service.dto.Person;

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
	public void remove(long id) {
		remove(id, "delete from person where id = ?");
	}

	@Override
	public Person get(long id) {
		return get(id, "select * from person where id = ?");
	}

	@Override
	public Person[] getAll() {
		return getAll("select * from person order by id").toArray(new Person[0]);
	}

	@Override
	public void update(Person d) {
		update(d, "update person set first_name = ?, last_name = ? where id = ?");
	}
}
