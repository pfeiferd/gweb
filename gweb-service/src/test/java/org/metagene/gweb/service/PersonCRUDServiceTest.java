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

import org.metagene.gweb.service.dto.Person;

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
