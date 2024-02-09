package org.metagene.genestrip.service.rest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.servlet.ServletConfig;
import javax.sql.DataSource;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.PersonService;
import org.metagene.genestrip.service.ServiceException;
import org.metagene.genestrip.service.UserService;
import org.metagene.genestrip.service.create.ServiceCreator;
import org.metagene.genestrip.service.create.ServiceCreator.Config;

public class RestApplication extends Application {
	public static final String DATA_SOURCE = "dataSource";
	public static final String HASH_PASSWORD = "hashPassword";

	private final Set<Object> services;
	private final Set<Class<?>> classes;

	public RestApplication(@javax.ws.rs.core.Context ServletConfig servletContext) {
		classes = new HashSet<Class<?>>();
		classes.add(RestExceptionMapper.class);

		try {
			Context envContext = (Context) new InitialContext().lookup("java:/comp/env");
			String dsName = servletContext.getInitParameter(DATA_SOURCE);
			DataSource dataSource = (DataSource) envContext.lookup(dsName);
			try {
				envContext.close();
			} catch (OperationNotSupportedException e) {
				// Ignore on purpose.
			}

			ServiceCreator serviceCreator = new ServiceCreator(dataSource, new Config() {
				@Override
				public String getConfigValue(String param) {
					return servletContext.getInitParameter(param);
				}
			});
						
			services = new HashSet<Object>();
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

		} catch (NamingException e) {
			throw new ServiceException(e);
		}
	}

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
		@Override
		public Response toResponse(Throwable exception) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(os);
			exception.printStackTrace(ps);
			String stackTrace = null;
			try {
				stackTrace = os.toString("UTF8");
			} catch (UnsupportedEncodingException e) {
				stackTrace = e.toString();
			}
			ResponseBuilder builder = exception instanceof NotFoundException
					? Response.status(Response.Status.NOT_FOUND)
					: Response.serverError();
			return builder.entity(stackTrace).type(MediaType.APPLICATION_JSON).build();
		}
	}
}
