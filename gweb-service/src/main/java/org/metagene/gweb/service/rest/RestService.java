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

import org.metagene.gweb.service.Service;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.role.UserStore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

public abstract class RestService<S extends Service> {
	public static final String APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON + "; charset=UTF-8";
	public static final String TEXT_PLAIN_UTF8 = MediaType.TEXT_PLAIN + "; charset=UTF-8";
	public static final String USER_ATTR = "user";

	@Context
	private HttpServletRequest request;

	private final S delegate;
	private final UserStore userStore;

	public RestService() {
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
	
	protected Response sendFile(File file) {
		if (file != null) {
			ResponseBuilder response = Response.ok((Object) file);
			response.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			return response.build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();		
	}
}
