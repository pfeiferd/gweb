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
package org.metagene.gweb.service;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Person;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.role.UserStore;

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

		userId = serviceCreator.getBasicService(UserService.class).getAll()[0].getId();
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
		Job job = new Job();
		job.setFastqFile(nextString());
		job.setDbId(dbId);
		job.setUserId(userId);
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
		Person admin = personService.getAll()[0];
		assertEquals("Admin", admin.getFirstName());
		assertEquals("Admin", admin.getLastName());

		JobService jobService = serviceCreator.createRoleService(JobService.class, testUserStore);
		Job job = newDTO();
		long jobId = jobService.create(job);
		assertEquals(JobStatus.ENQUEUED, jobService.enqueue(jobId));

		//assertEquals(jobId, (long) jobService.getJobIdsByStatus(JobStatus.STARTED)[0]);
//
//		System.out.println("Wait 10s...");
//		Thread.sleep(10000);
//
//		assertEquals(JobStatus.FINISHED, jobService.get(jobId).getStatus());
//		assertTrue(jobService.getJobIdsByStatus(JobStatus.STARTED).length == 0);
	}
}
