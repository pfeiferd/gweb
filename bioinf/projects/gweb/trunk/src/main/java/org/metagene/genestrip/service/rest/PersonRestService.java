package org.metagene.genestrip.service.rest;

import javax.ws.rs.Path;

import org.metagene.genestrip.service.PersonService;
import org.metagene.genestrip.service.dto.Person;

@Path("/PersonService")
public abstract class PersonRestService extends CRUDRestService<PersonService, Person> implements PersonService {
}
