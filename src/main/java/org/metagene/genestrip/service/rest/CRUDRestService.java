package org.metagene.genestrip.service.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.metagene.genestrip.service.CRUDService;
import org.metagene.genestrip.service.dto.DTO;
import org.metagene.genestrip.service.dto.User;
import org.metagene.genestrip.service.role.UserStore;

public abstract class CRUDRestService<S extends CRUDService<D>, D extends DTO> implements CRUDService<D> {
	public static final String USER_ATTR = "user";

	@Context
	private HttpServletRequest request;

	private final S delegate;
	private final UserStore userStore;

	public CRUDRestService() {
		this.userStore = new UserStore() {
			@Override
			public void setUser(User u) {
				HttpSession session = request.getSession(false);
				if (u == null && session != null) {
					session.invalidate();
				} else {
					request.getSession(true).setAttribute(USER_ATTR, u);
				}
			}

			@Override
			public User getUser() {
				HttpSession session = request.getSession(false);
				return session == null ? null : (User) session.getAttribute(USER_ATTR);
			}
		};
		this.delegate = createDelegate();
	}

	protected UserStore getUserStore() {
		return userStore;
	}

	protected abstract S createDelegate();

	protected S getDelegate() {
		return delegate;
	}

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
	@Produces(MediaType.APPLICATION_JSON)
	public boolean remove(@PathParam("id") long id) {
		return getDelegate().remove(id);
	}

	@Override
	@GET
	@Path("get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public D get(@PathParam("id") long id) {
		return getDelegate().get(id);
	}

	@Override
	@GET
	@Path("getAll")
	@Produces(MediaType.APPLICATION_JSON)
	public List<D> getAll() {
		return getDelegate().getAll();
	}

	@Override
	@POST
	@Path("update")
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean update(D d) {
		return getDelegate().update(d);
	}
}
