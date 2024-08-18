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
package org.metagene.gweb.service.rest;


import org.metagene.gweb.service.CRUDService;
import org.metagene.gweb.service.dto.DTO;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public abstract class CRUDRestService<S extends CRUDService<D>, D extends DTO> extends RestService<S>  implements CRUDService<D> {	
	@Override
	@POST
	@Path("create")
	@Consumes(MediaType.APPLICATION_JSON)
	public long create(D d) {
		return getDelegate().create(d);
	}

	@Override
	@GET
	@Path("remove/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public void remove(@PathParam("id") long id) {
		getDelegate().remove(id);
	}

	@Override
	@GET
	@Path("get/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public D get(@PathParam("id") long id) {
		return getDelegate().get(id);
	}

	@Override
	@GET
	@Path("getAll")
	@Produces(APPLICATION_JSON_UTF8)
	public D[] getAll() {
		return getDelegate().getAll();
	}

	@Override
	@POST
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	public void update(D d) {
		getDelegate().update(d);
	}
}
