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


import java.io.File;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.dto.DB;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/DBService")
public abstract class DBRestService extends CRUDRestService<DBService, DB> implements DBService {
	@Override
	public File getInfoFile(long dbId) {
		return getDelegate().getInfoFile(dbId);
	}

	@GET
	@Path("getInfo/{id}")
	@Produces(TEXT_PLAIN_UTF8)
	public Response getInfo(@PathParam("id") long jobId) {
		return sendFile(getInfoFile(jobId));
	}
}
