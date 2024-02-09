package org.metagene.genestrip.service.create;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.InstallService;
import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.PersonService;
import org.metagene.genestrip.service.UserService;
import org.metagene.genestrip.service.compute.DBComputeService;
import org.metagene.genestrip.service.compute.JobComputeService;
import org.metagene.genestrip.service.dto.Person;
import org.metagene.genestrip.service.dto.User;
import org.metagene.genestrip.service.jdbc.DBJDBCService;
import org.metagene.genestrip.service.jdbc.InstallJDBCService;
import org.metagene.genestrip.service.jdbc.JobJDBCService;
import org.metagene.genestrip.service.jdbc.PersonJDBCService;
import org.metagene.genestrip.service.jdbc.UserJDBCService;
import org.metagene.genestrip.service.role.DBRoleService;
import org.metagene.genestrip.service.role.JobRoleService;
import org.metagene.genestrip.service.role.PersonRoleService;
import org.metagene.genestrip.service.role.UserRoleService;
import org.metagene.genestrip.service.role.UserStore;

public class ServiceCreator {
	public static final String HASH_PASSWORD = "hashPassword";
	public static final String JOB_DELAY = "jobDelay";
	public static final String JOB_PERIOD = "jobPeriod";
	public static final String INIT_ADMIN = "initAdmin";

	private final DataSource dataSource;
	private final Config config;
	private final Map<Class<?>, Object> basicServices;

	public ServiceCreator(DataSource dataSource, Config config) {
		this.dataSource = dataSource;
		this.config = config;
		this.basicServices = new HashMap<Class<?>, Object>();
		createBasicServices();
	}

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
		Boolean hash = Boolean.valueOf(config.getConfigValue(HASH_PASSWORD, true));
		Long delay = Long.valueOf(config.getConfigValue(JOB_DELAY, 1000));
		Long period = Long.valueOf(config.getConfigValue(JOB_PERIOD, 10000));
		Boolean initAdmin = Boolean.valueOf(config.getConfigValue(INIT_ADMIN, true));

		InstallService installService = new InstallJDBCService(dataSource);
		installService.install();
		
		PersonService ps = new PersonJDBCService(dataSource);
		UserService us = new UserJDBCService(dataSource, hash);
		
		if (initAdmin && us.getAll().isEmpty()) {
			Person person = new Person();
			person.setFirstName("Admin");
			person.setLastName("Admin");
			long personId = ps.create(person);
			
			User user = new User();
			user.setAllowAll(true);
			user.setAllowJobs(true);
			user.setLogin("admin");
			user.setPassword("admin");
			user.setPersonId(personId);			
			us.create(user);
		}

		basicServices.put(PersonService.class, ps);
		basicServices.put(UserService.class, us);
		basicServices.put(DBService.class, new DBComputeService(new DBJDBCService(dataSource)));
		basicServices.put(JobService.class, new JobComputeService(new JobJDBCService(dataSource), delay, period));
	}

	@SuppressWarnings("unchecked")
	public <T> T getBasicService(Class<T> clazz) {
		return (T) basicServices.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T createRoleService(Class<T> clazz, UserStore userStore) {
		if (clazz.equals(PersonService.class)) {
			return (T) new PersonRoleService(getBasicService(PersonService.class), userStore);
		} else if (clazz.equals(DBService.class)) {
			return (T) new DBRoleService(getBasicService(DBService.class), userStore);
		} else if (clazz.equals(UserService.class)) {
			return (T) new UserRoleService(getBasicService(UserService.class), userStore);
		} else if (clazz.equals(JobService.class)) {
			return (T) new JobRoleService(getBasicService(JobService.class), userStore);
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
}
