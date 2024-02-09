package org.metagene.genestrip.service.role;

import java.util.List;

import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.dto.JobProgress;
import org.metagene.genestrip.service.dto.JobResult;

public class JobRoleService extends CRUDRoleService<JobService, Job> implements JobService {
	public JobRoleService(JobService delegate, UserStore userStore) {
		super(delegate, userStore);
	}

	@Override
	public long create(Job d) {
		checkJobsAllowed();
		if (!(getLoggedInUser().isAllowAll() || d.getUserId() == getLoggedInUser().getId())) {
			throw new MissingRightException("Job access right for job missing in role.");			
		}

		return getDelegate().create(d);
	}

	@Override
	public boolean remove(long id) {
		checkJobsAllowed();
		checkIsMyJob(id);
		return getDelegate().remove(id);
	}

	@Override
	public Job get(long id) {
		checkReadAllowed();
		checkIsMyJob(id);
		return getDelegate().get(id);
	}

	@Override
	public boolean update(Job d) {
		checkAllAllowed();
		checkIsMyJob(d.getId());
		return getDelegate().update(d);
	}	
	
	@Override
	public JobStatus enqueue(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().enqueue(jobId);
	}

	@Override
	public JobStatus cancel(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().enqueue(jobId);
	}

	@Override
	public List<Job> getByUser(long userId) {
		checkReadAllowed();
		return getDelegate().getByUser(userId);
	}

	@Override
	public JobResult getResult(long jobId) {
		checkReadAllowed();
		checkIsMyJob(jobId);
		return getDelegate().getResult(jobId);
	}

	@Override
	public JobProgress getProgress(long jobId) {
		checkJobsAllowed();
		checkIsMyJob(jobId);
		return getDelegate().getProgress(jobId);
	}

	protected void checkIsMyJob(long jobId) {
		if (getLoggedInUser().isAllowAll()) {
			return;
		} else {
			Job job = getDelegate().get(jobId);
			if (job != null) {
				if (job.getUserId() != getLoggedInUser().getId()) {
					throw new MissingRightException("Job access right for job missing in role.");
				}
			}
		}
	}
	
	@Override
	public List<Long> getPendingJobIds() {
		checkReadAllowed();
		List<Long> res = getDelegate().getPendingJobIds();
		if (getLoggedInUser().isAllowAll()) {
			return res;
		} else {
			List<Job> jobs = getDelegate().getByUser(getLoggedInUser().getId());
			for (int i = 0; i < res.size(); i++) {
				boolean found = false;
				for (Job job : jobs) {
					if (job.getId() == res.get(i)) {
						found = true;
						break;
					}
				}
				if (!found) {
					res.set(i, null);
				}
			}
			return res;
		}
	}
	
	@Override
	public List<Long> getRunningJobIds() {
		checkReadAllowed();
		List<Long> res = getDelegate().getRunningJobIds();
		
		List<Job> jobs = getDelegate().getByUser(getLoggedInUser().getId());
		for (int i = 0; i < res.size(); i++) {
			boolean found = false;
			for (Job job : jobs) {
				if (job.getId() == res.get(i)) {
					found = true;
					break;
				}
			}
			if (!found) {
				res.set(i, null);
			}
		}
		
		return res;
	}
}
