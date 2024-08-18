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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.sql.DataSource;

import org.metagene.gweb.service.CannotRemoveException;
import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.FastqFileService;
import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.NotFoundException;
import org.metagene.gweb.service.PersonService;
import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.ServiceException;
import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.ValidationException;
import org.metagene.gweb.service.create.ServiceCreator;
import org.metagene.gweb.service.create.ServiceCreator.Config;
import org.metagene.gweb.service.create.ServiceCreator.Logger;
import org.metagene.gweb.service.role.MissingRightException;

import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

public abstract class RestApplication extends Application {
	private static boolean applyFixForJaxrsStringResultBug = true;
	
	public static void setApplyFixForJaxrsStringResultBug(boolean applyFixForJaxrsStringResultBug) {
		RestApplication.applyFixForJaxrsStringResultBug = applyFixForJaxrsStringResultBug;
	}
	
	public static boolean isApplyFixForJaxrsStringResultBug() {
		return applyFixForJaxrsStringResultBug;
	}
	
	public static final String TEST_MODE = "testMode";
	public static final String DATA_SOURCE = "dataSource";
	public static final String HASH_PASSWORD = "hashPassword";

	private final Set<Object> services;
	private final Set<Class<?>> classes;

	public RestApplication(@jakarta.ws.rs.core.Context ServletConfig servletConfig) {
		String testModeStr = servletConfig.getInitParameter(TEST_MODE);
		boolean testMode = Boolean.valueOf(testModeStr);
		services = new HashSet<Object>();
		services.add(new RestExceptionMapper(testMode));

		classes = new HashSet<Class<?>>();

		try {
			Context envContext = (Context) new InitialContext().lookup("java:/comp/env");
			String dsName = servletConfig.getInitParameter(DATA_SOURCE);
			DataSource dataSource = (DataSource) envContext.lookup(dsName);
			try {
				envContext.close();
			} catch (OperationNotSupportedException e) {
				// Ignore on purpose.
			}

			ServiceCreator serviceCreator = createServiceCreator(dataSource, new Config() {
				@Override
				public String getConfigValue(String param) {
					return servletConfig.getInitParameter(param);
				}
			}, new Logger() {
				@Override
				public void log(String message) {
					servletConfig.getServletContext().log(message);
				}
				
				@Override
				public void log(String message, Throwable t) {
					servletConfig.getServletContext().log(message, t);
				}
			});

			services.add(new PersonRestService() {
				@Override
				protected PersonService createDelegate() {
					return serviceCreator.createRoleService(PersonService.class, getUserStore());
				}
			});
			services.add(new UserRestService() {
				@Override
				protected UserService createDelegate() {
					return serviceCreator.createRoleService(UserService.class, getUserStore());
				}
			});
			services.add(new DBRestService() {
				@Override
				protected DBService createDelegate() {
					return serviceCreator.createRoleService(DBService.class, getUserStore());
				}
			});
			services.add(new JobRestService() {
				@Override
				protected JobService createDelegate() {
					return serviceCreator.createRoleService(JobService.class, getUserStore());
				}
			});
			services.add(new FastqFileRestService() {
				@Override
				protected FastqFileService createDelegate() {
					return serviceCreator.createRoleService(FastqFileService.class, getUserStore());
				}
			});
			services.add(new ResourceRestService() {
				@Override
				protected ResourceService createDelegate() {
					return serviceCreator.createRoleService(ResourceService.class, getUserStore());
				}
			});

		} catch (NamingException e) {
			throw new ServiceException(e);
		}
	}

	protected abstract ServiceCreator createServiceCreator(DataSource dataSource, Config config, Logger logger);

	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return services;
	}

	@Provider
	public static class RestExceptionMapper implements ExceptionMapper<Throwable> {
		private final boolean stackTrace;

		public RestExceptionMapper(boolean stackTrace) {
			this.stackTrace = stackTrace;
		}

		@Override
		public Response toResponse(Throwable exception) {
			String errorInfo;
			if (stackTrace) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(os);
				exception.printStackTrace(ps);
				try {
					errorInfo = os.toString("UTF8");
				} catch (UnsupportedEncodingException e) {
					errorInfo = e.toString();
				}
			} else {
				errorInfo = exception.getClass().getName();
			}
			ResponseBuilder builder;
			if (exception instanceof jakarta.ws.rs.NotFoundException) {
				builder = Response.status(Response.Status.NOT_FOUND);
			} else if (exception instanceof MissingRightException) {
				builder = Response.status(Response.Status.UNAUTHORIZED);
			} else if (exception instanceof NotFoundException) {
				builder = Response.status(Response.Status.GONE);
			} else if (exception instanceof CannotRemoveException) {
				builder = Response.status(Response.Status.CONFLICT);
			} else if (exception instanceof ValidationException) {
				builder = Response.status(Response.Status.FORBIDDEN);
			} else {
				builder = Response.serverError();
			}
			return builder.entity(errorInfo).type(RestService.APPLICATION_JSON_UTF8).build();
		}
	}
		
	public static String fixForJaxrsStringResultBug(String s) {		
		return isApplyFixForJaxrsStringResultBug() ? "\"" + RestApplication.encodeStrForJS(s)  + "\"" : s;
	}
	
	public static String encodeStrForJS(String s) {
		if (s == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '\'' || c == '"') {
				buffer.append('\\');
			}
			buffer.append(c);
		}

		return buffer.toString();
	}
}
