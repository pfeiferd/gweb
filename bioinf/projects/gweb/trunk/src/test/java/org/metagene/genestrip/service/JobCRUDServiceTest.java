package org.metagene.genestrip.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Person;
import org.metagene.genestrip.service.dto.User;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.role.UserStore;

public class JobCRUDServiceTest extends AbstractCRUDServiceTest<JobService, Job> {
	private long dbId;
	private long userId;

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();

		DBService dbService = serviceCreator.getBasicService(DBService.class);
		DB db = new DB();
		db.setName(nextString());
		db.setDbFilePrefix(nextString());
		dbId = dbService.create(db);

		userId = serviceCreator.getBasicService(UserService.class).getAll().get(0).getId();
	}

	protected boolean isInitAdmin() {
		return true;
	}

	protected boolean isWithUpdate() {
		return false;
	}

	@Override
	protected boolean dtosEqual(Job d1, Job d2) {
		return d1.getName().equals(d2.getName()) && d1.getDbId() == d2.getDbId() && d1.getUserId() == d2.getUserId()
				&& d1.getFastqFile().equals(d2.getFastqFile());
	}

	@Override
	protected Job newDTO() {
		Job job = new Job(nextString(), null, dbId, userId);
		job.setName(nextString());
		return job;
	}

	@Override
	protected Class<JobService> getServiceClass() {
		return JobService.class;
	}

	@Override
	protected void updateDTO(Job d) {
		d.setName(nextString());
	}

	@Test
	public void testCreateAndRunJob() throws SQLException, InterruptedException {
		UserStore testUserStore = new UserStore() {
			private User user;

			@Override
			public void setUser(User u) {
				this.user = u;
			}

			@Override
			public User getUser() {
				return user;
			}
		};

		UserService userService = serviceCreator.createRoleService(UserService.class, testUserStore);
		userService.login("admin", "admin");
		assertEquals("admin", testUserStore.getUser().getLogin());
		assertEquals(null, testUserStore.getUser().getPassword());

		PersonService personService = serviceCreator.createRoleService(PersonService.class, testUserStore);
		Person admin = personService.getAll().get(0);
		assertEquals("Admin", admin.getFirstName());
		assertEquals("Admin", admin.getLastName());

		JobService jobService = serviceCreator.createRoleService(JobService.class, testUserStore);
		Job job = newDTO();
		long jobId = jobService.create(job);
		assertEquals(JobStatus.ENQUEUED, jobService.enqueue(jobId));

		assertEquals(jobId, (long) jobService.getPendingJobIds().get(0));

		System.out.println("Wait 10s...");
		Thread.sleep(10000);

		assertEquals(JobStatus.FINISHED, jobService.get(jobId).getStatus());
		assertTrue(jobService.getPendingJobIds().isEmpty());
	}
}
