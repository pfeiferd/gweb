package org.metagene.genestrip.service.role;

import org.metagene.genestrip.service.PersonService;
import org.metagene.genestrip.service.dto.Person;

public class PersonRoleService extends CRUDRoleService<PersonService, Person> implements PersonService {
	public PersonRoleService(PersonService delegate, UserStore userStore) {
		super(delegate, userStore);
	}
}
