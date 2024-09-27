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
package org.metagene.gweb.service.create;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.FastqFileService;
import org.metagene.gweb.service.InstallService;
import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.PersonService;
import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.compute.DBComputeService;
import org.metagene.gweb.service.compute.FastqFileComputeService;
import org.metagene.gweb.service.compute.JobComputeService;
import org.metagene.gweb.service.compute.JobExecutable;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Person;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.dto.User.UserRole;
import org.metagene.gweb.service.jdbc.DBJDBCService;
import org.metagene.gweb.service.jdbc.InstallJDBCService;
import org.metagene.gweb.service.jdbc.InstallJDBCService.SQLDialect;
import org.metagene.gweb.service.jdbc.JobJDBCService;
import org.metagene.gweb.service.jdbc.PersonJDBCService;
import org.metagene.gweb.service.jdbc.ResourceJDBCService;
import org.metagene.gweb.service.jdbc.UserJDBCService;
import org.metagene.gweb.service.role.DBRoleService;
import org.metagene.gweb.service.role.FastqFileRoleService;
import org.metagene.gweb.service.role.JobRoleService;
import org.metagene.gweb.service.role.PersonRoleService;
import org.metagene.gweb.service.role.ResourceRoleService;
import org.metagene.gweb.service.role.UserRoleService;
import org.metagene.gweb.service.role.UserStore;

public abstract class ServiceCreator {
	public static final String LOCAL_INSTALL = "localInstall";
	public static final String INIT_DEFAULT_USER = "initDefaultUser";
	public static final String DEFAULT_USER = "defaultUser";
	public static final String JOB_DELAY = "jobDelay";
	public static final String JOB_PERIOD = "jobPeriod";
	public static final String INIT_ADMIN = "initAdmin";
	public static final String GENESTRIP_BASE_DIR = "genestripBaseDir";
	public static final String SQL_DIALECT = "sqlDialect";
	public static final String THREADS = "threads";
	public static final String INIT_DBS = "initDBs";
	public static final String FILE_PATH_ROLE = "filePathRole";
	// Has not impact yet, therefore not document. (Impact only on log level trace, but GWeb uses log level info)
	public static final String LOG_PROGRESS = "logProgressUpdateCycle";

	private final boolean localInstall;
	private final DataSource dataSource;
	private final Config config;
	private final Logger logger;
	private final Map<Class<?>, Object> basicServices;

	public ServiceCreator(DataSource dataSource, Config config, Logger logger) {
		this.localInstall = Boolean.valueOf(config.getConfigValue(LOCAL_INSTALL, false));
		this.dataSource = dataSource;
		this.config = config;
		this.logger = logger;
		this.basicServices = new HashMap<Class<?>, Object>();

		createGSConfig(new File(config.getConfigValue(GENESTRIP_BASE_DIR, "./data")));
		createBasicServices();
	}

	protected final Logger getLogger() {
		return logger;
	}

	protected abstract void createGSConfig(File genestripbaseDir);

	protected abstract File getFastqDir();

	protected abstract File getLogBaseDir(String projectName);

	protected abstract File getCSVBaseDir(String projectName);

	protected abstract File getDBFile(String projectName);

	protected abstract File getDBInfoFile(String projectName);

	protected DataSource getDataSource() {
		return dataSource;
	}

	protected Config getConfig() {
		return config;
	}

	protected Map<Class<?>, ?> getBasicServices() {
		return basicServices;
	}

	protected void createBasicServices() {
		InstallService installService = createBasicInstallService(dataSource,
				config.getConfigValue(SQL_DIALECT, SQLDialect.POSTGRES));
		boolean freshInstall = installService.install();

		PersonService ps = new PersonJDBCService(dataSource);
		basicServices.put(PersonService.class, ps);

		UserService us = new UserJDBCService(dataSource);
		basicServices.put(UserService.class, us);

		boolean initAdmin = Boolean.parseBoolean(config.getConfigValue(INIT_ADMIN, true));
		if (initAdmin) {
			if (us.getByLogin("admin") == null) {
				Person person = new Person();
				person.setFirstName("Admin");
				person.setLastName("Admin");
				long personId = ps.create(person);

				User user = new User();
				user.setRole(UserRole.ADMIN);
				user.setLogin("admin");
				user.setPassword("admin");
				user.setPersonId(personId);
				us.create(user);
			}
		}

		boolean initDefaultUser = Boolean.parseBoolean(config.getConfigValue(INIT_DEFAULT_USER, true));
		if (initDefaultUser) {
			String defaultUser = config.getConfigValue(DEFAULT_USER);
			if (defaultUser != null && us.getByLogin(defaultUser) == null) {
				Person person = new Person();
				person.setFirstName("User");
				person.setLastName("User");
				long personId = ps.create(person);

				User user = new User();
				user.setRole(UserRole.RUN_JOBS);
				user.setLogin(defaultUser);
				user.setPassword(defaultUser);
				user.setPersonId(personId);
				us.create(user);
			}
		}

		DBService dbJdbcService = new DBJDBCService(dataSource);
		basicServices.put(DBService.class, new DBComputeService(dbJdbcService) {
			@Override
			protected File getDBFile(String projectName) {
				return ServiceCreator.this.getDBFile(projectName);
			}

			@Override
			protected File getDBInfoFile(String projectName) {
				return ServiceCreator.this.getDBInfoFile(projectName);
			}
		});

		if (freshInstall) {
			boolean initDBs = Boolean.parseBoolean(config.getConfigValue(INIT_DBS, true));
			if (initDBs) {
				new StandardDBsInstaller(dbJdbcService, getLogger()).install();
			}
		}

		long delay = Long.parseLong(config.getConfigValue(JOB_DELAY, 1000));
		long period = Long.parseLong(config.getConfigValue(JOB_PERIOD, 10000));

		JobService jobService = new JobJDBCService(dataSource);
		long[] runningJobIds = jobService.getJobIdsByStatus(JobStatus.STARTED);
		for (long id : runningJobIds) {
			jobService.cancel(id);
		}
		basicServices.put(FastqFileService.class, new FastqFileComputeService(getFastqDir(), us));

		ResourceService resourceService = new ResourceJDBCService(dataSource);
		basicServices.put(ResourceService.class, resourceService);

		basicServices.put(JobService.class, new JobComputeService(jobService, dbJdbcService, resourceService, delay,
				period, createsJobExecutableFactory()) {
			@Override
			protected File getLogBaseDir(String projectName) {
				return ServiceCreator.this.getLogBaseDir(projectName);
			}

			@Override
			protected File getCSVBaseDir(String projectName) {
				return ServiceCreator.this.getCSVBaseDir(projectName);
			}
		});
	}

	protected abstract JobExecutable.Factory createsJobExecutableFactory();

	@SuppressWarnings("unchecked")
	public <T> T getBasicService(Class<T> clazz) {
		return (T) basicServices.get(clazz);
	}

	protected InstallService createBasicInstallService(DataSource dataSource, String sqlDialect) {
		return new InstallJDBCService(dataSource, SQLDialect.valueOf(sqlDialect));
	}

	@SuppressWarnings("unchecked")
	public <T> T createRoleService(Class<T> clazz, UserStore userStore) {
		if (clazz.equals(PersonService.class)) {
			return (T) new PersonRoleService(getBasicService(PersonService.class), userStore);
		} else if (clazz.equals(DBService.class)) {
			return (T) new DBRoleService(getBasicService(DBService.class), userStore);
		} else if (clazz.equals(UserService.class)) {
			return (T) new UserRoleService(getBasicService(UserService.class), userStore,
					config.getConfigValue(DEFAULT_USER));
		} else if (clazz.equals(JobService.class)) {
			return (T) new JobRoleService(getBasicService(JobService.class), userStore);
		} else if (clazz.equals(FastqFileService.class)) {
			return (T) new FastqFileRoleService(getBasicService(FastqFileService.class), userStore, localInstall);
		} else if (clazz.equals(ResourceService.class)) {
			UserRole role = null;
			try {
				role = UserRole.valueOf(config.getConfigValue(FILE_PATH_ROLE));
				if (!role.subsumes(UserRole.RUN_JOBS)) {
					role = null;
				}
			} catch (Exception e) {
				// Ignore on purpose.
			}

			return (T) new ResourceRoleService(getBasicService(ResourceService.class), userStore, role);
		}
		throw new IllegalArgumentException("Unknown service class " + clazz);
	}

	public interface Config {
		public String getConfigValue(String param);

		default public String getConfigValue(String param, Object defaultValue) {
			String v = getConfigValue(param);
			return v == null ? (defaultValue == null ? null : defaultValue.toString()) : v;
		}
	}

	public interface Logger {
		public void log(String message);

		public void log(String message, Throwable t);
	}
}
