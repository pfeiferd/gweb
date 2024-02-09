package org.metagene.genestrip.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.DBInfo;

@Path("/DBService")
public abstract class DBRestService extends CRUDRestService<DBService, DB> implements DBService {
	@Override
	@GET
	@Path("getDBInfo/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public DBInfo getDBInfo(long dbID) {
		return getDelegate().getDBInfo(dbID);
	}
}
