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
package org.metagene.gweb.service;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.metagene.gweb.service.dto.Person;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.dto.User.UserRole;

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
		d.setRole(UserRole.VIEW);
	}

	@Override
	protected Class<UserService> getServiceClass() {
		return UserService.class;
	}
	
	@Test
	public void testLogin() {
		User u = newDTO();
		
		UserService service = serviceCreator.getBasicService(getServiceClass());
		
		service.create(u);
		User u2 = service.login(u.getLogin(), u.getPassword());

		dtosEqual(u, u2);
	}
}
