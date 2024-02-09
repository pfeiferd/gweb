package org.metagene.genestrip.service.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.metagene.genestrip.service.UserService;
import org.metagene.genestrip.service.dto.User;

@Path("/UserService")
public abstract class UserRestService extends CRUDRestService<UserService, User> implements UserService {
	@Override
	// TODO: Better remove this (@GET) for security reasons. Nice for testing though.
	@GET 
	@POST
	@Path("login/{login}/{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public User login(@PathParam("login") String login,
			@PathParam("password") String password) {
		return getDelegate().login(login, password);
	}

	@Override
	@GET
	@Path("logout")
	@Produces(MediaType.APPLICATION_JSON)
	public void logout() {
		getDelegate().logout();
	}
}
