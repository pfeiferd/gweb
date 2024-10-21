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
package org.metagene.gweb.service.role;

import java.io.File;
import java.util.Arrays;

import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.dto.DTO;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.JobProgress;
import org.metagene.gweb.service.dto.User.UserRole;

public class JobRoleService extends CRUDRoleService<JobService, Job> implements JobService {
	public JobRoleService(JobService delegate, UserStore userStore) {
		super(delegate, userStore);
	}

	@Override
	public long create(Job d) {
		checkJobsAllowed();
		checkIsMyJob(d);

		return getDelegate().create(d);
	}

	@Override
	public void remove(long id) {
		checkJobsAllowed();
		checkIsMyJob(id);
		getDelegate().remove(id);
	}

	@Override
	public Job get(long id) {
		checkReadAllowed();
		checkIsMyJob(id);
		return getDelegate().get(id);
	}

	@Override
	public void update(Job d) {
		checkJobsAllowed();
		checkIsMyJob(d.getId());
		getDelegate().update(d);
	}

	@Override
	public JobStatus enqueue(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().enqueue(jobId);
	}

	@Override
	public long enqueueDBInfo(long dbId, long userId) {
		checkAllAllowed();
		if (userId != getLoggedInUser().getId()) {
			throw new MissingRightException("User id and logged in user id must match for this.");
		}
		return getDelegate().enqueueDBInfo(dbId, userId);
	}

	@Override
	public long enqueueDBInstall(long dbId, long userId) {
		checkAllAllowed();
		if (userId != getLoggedInUser().getId()) {
			throw new MissingRightException("User id and logged in user id must match for this.");
		}
		return getDelegate().enqueueDBInstall(dbId, userId);
	}
	
	@Override
	public JobStatus cancel(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().cancel(jobId);
	}

	@Override
	public Job[] getByUser(long userId) {
		checkReadAllowed();
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			if (userId != getLoggedInUser().getId()) {
				throw new MissingRightException("Job access right for jobs missing in role.");
			}
		}
		
		return getDelegate().getByUser(userId);
	}
	
	@Override
	public JobProgress getProgress(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().getProgress(jobId);
	}

	protected void checkIsMyJob(long jobId) {
		if (getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			return;
		} else {
			Job job = getDelegate().get(jobId);
			if (job != null) {
				checkIsMyJob(job);
			}
		}
	}
	
	@Override
	public long[] getActiveJobIds() {
		checkReadAllowed();
		return filterIdsByUser(getDelegate().getActiveJobIds());
	}

	@Override
	public long[] getJobIdsByStatus(JobStatus status) {
		checkReadAllowed();
		return filterIdsByUser(getDelegate().getJobIdsByStatus(status));
	}

	protected long[] filterIdsByUser(long[] res) {
		if (getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			return res;
		}

		Job[] jobs = getDelegate().getByUser(getLoggedInUser().getId());

		Job j = new Job();
		for (int i = 0; i < res.length; i++) {
			j.setId(res[i]);
			int index = Arrays.binarySearch(jobs, j, ID_DTO_COMPARATOR);
			if (index == -1) {
				res[i] = DTO.INVALID_ID;
			}
		}

		return res;
	}

	@Override
	public JobStatus[] getStatusForJobs(long[] jobIds) {
		checkReadAllowed();

		JobStatus[] res = getDelegate().getStatusForJobs(jobIds);
		long[] ids = filterIdsByUser(jobIds);

		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == DTO.INVALID_ID) {
				res[i] = null;
			}
		}

		return res;
	}

	@Override
	public boolean isLogExists(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		
		return getDelegate().isLogExists(jobId);
	}

	@Override
	public boolean isCSVExists(long jobId) {
		checkIsMyJob(jobId);
		
		return getDelegate().isCSVExists(jobId);
	}
		
	@Override
	public File getLogFile(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);

		return getDelegate().getLogFile(jobId);
	}
	
	@Override
	public File getCSVFile(long jobId) {
		checkIsMyJob(jobId);
		
		return getDelegate().getCSVFile(jobId);
	}
}
