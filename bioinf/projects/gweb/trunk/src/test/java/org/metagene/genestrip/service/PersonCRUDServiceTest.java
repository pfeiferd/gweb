package org.metagene.genestrip.service;

import org.metagene.genestrip.service.dto.Person;

public class PersonCRUDServiceTest extends AbstractCRUDServiceTest<PersonService, Person> {
	@Override
	protected boolean dtosEqual(Person d1, Person d2) {
		return d1.getFirstName().equals(d2.getFirstName()) && d1.getLastName().equals(d2.getLastName());
	}
	
	@Override
	protected Person newDTO() {
		Person p = new Person();
		updateDTO(p);
		
		return p;
	}
	
	@Override
	protected void updateDTO(Person d) {
		d.setFirstName(nextString());
		d.setLastName(nextString());
	}

	@Override
	protected Class<PersonService> getServiceClass() {
		return PersonService.class;
	}
}
