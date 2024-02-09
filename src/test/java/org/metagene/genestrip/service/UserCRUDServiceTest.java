package org.metagene.genestrip.service;

import java.sql.SQLException;

import org.junit.Before;
import org.metagene.genestrip.service.dto.Person;
import org.metagene.genestrip.service.dto.User;

public class UserCRUDServiceTest extends AbstractCRUDServiceTest<UserService, User> {
	private long personId;
	
	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();
		Person p = new Person();
		p.setFirstName(nextString());
		p.setLastName(nextString());
		
		personId = serviceCreator.getBasicService(PersonService.class).create(p);
	}
	
	@Override
	protected boolean dtosEqual(User d1, User d2) {
		return d1.getLogin().equals(d2.getLogin()) && d1.getPersonId() == d2.getPersonId();
	}
	
	@Override
	protected User newDTO() {
		User u = new User();
		updateDTO(u);
		
		return u;
	}
	
	@Override
	protected void updateDTO(User d) {
		d.setLogin(nextString());
		d.setPassword(nextString());
		d.setPersonId(personId);
	}

	@Override
	protected Class<UserService> getServiceClass() {
		return UserService.class;
	}
}
