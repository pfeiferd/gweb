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


import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.dto.User;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("/UserService")
public abstract class UserRestService extends CRUDRestService<UserService, User> implements UserService {
	@Override
	@POST
	@Path("login")
	@Produces(APPLICATION_JSON_UTF8)
	public User login(@FormParam("login") String login,
			@FormParam("password") String password) {
		return getDelegate().login(login, password);
	}

	// Only use this for testing. It is inherently insecure.
	@GET
	@Path("insecureLogin/{login}/{password}")
	@Produces(APPLICATION_JSON_UTF8)
	public User insecureLogin(@PathParam("login") String login,
			@PathParam("password") String password) {
		return getDelegate().login(login, password);
	}
	
	@Override
	@GET
	@Path("logout")
	@Produces(APPLICATION_JSON_UTF8)
	public void logout() {
		getDelegate().logout();
	}
	
	@Override
	@GET
	@Path("getLoggedInUser")
	@Produces(APPLICATION_JSON_UTF8)
	public User getLoggedInUser() {
		return getDelegate().getLoggedInUser();
	}

	// Not supported on REST level on purpose.
	@Override
	public User getByLogin(String login) {
		return null;
	}
}
