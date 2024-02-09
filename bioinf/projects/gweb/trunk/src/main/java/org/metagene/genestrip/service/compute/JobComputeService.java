package org.metagene.genestrip.service.compute;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.dto.JobProgress;
import org.metagene.genestrip.service.dto.JobResult;

public class JobComputeService implements JobService {
	private final TimerTask timerTask;
	private final Timer timer;
	
	private final JobService delegate;

	private boolean executing;
	private Job currentJob;

	public JobComputeService(JobService delegate, long delay, long period) {
		this.delegate = delegate;
		executing = false;
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				if (!executing) {
					List<Long> pendingJobs = delegate.getPendingJobIds();

					if (!pendingJobs.isEmpty()) {
						try {
							executing = true;
							currentJob = delegate.get(pendingJobs.get(0));
							currentJob.setStarted(new Date());
							currentJob.setStatus(JobStatus.STARTED);
							delegate.update(currentJob);
							execute(currentJob);
							currentJob.setFinished(new Date());
							currentJob.setStatus(JobStatus.FINISHED);
							delegate.update(currentJob);
						} finally {
							executing = false;
							currentJob = null;
						}
					}
				}
			}
		};
		timer.schedule(timerTask, delay, period);
	}

	@Override
	public List<Long> getPendingJobIds() {
		return delegate.getPendingJobIds();
	}

	protected void execute(Job job) {
		// TODO
		System.out.println("Executing " + job);
	}

	protected void stop(Job job) {
		// TODO
		System.out.println("Stopped " + job);
	}

	@Override
	public long create(Job d) {
		return delegate.create(d);
	}

	@Override
	public boolean remove(long id) {
		cancel(id);
		return delegate.remove(id);
	}

	@Override
	public Job get(long id) {
		return delegate.get(id);
	}

	@Override
	public List<Job> getAll() {
		return delegate.getAll();
	}

	@Override
	public boolean update(Job d) {
		throw new UnsupportedOperationException("Jobs cannot be updated after creation.");
	}

	@Override
	public JobStatus enqueue(long jobId) {
		return delegate.enqueue(jobId);
	}

	@Override
	public JobStatus cancel(long jobId) {
		if (currentJob != null && jobId == currentJob.getId()) {
			stop(currentJob);
			executing = false;
			currentJob = null;
		}
		return delegate.cancel(jobId);
	}

	@Override
	public List<Job> getByUser(long userId) {
		return delegate.getByUser(userId);
	}

	@Override
	public JobResult getResult(long jobId) {
		// TODO
		return null;
	}

	@Override
	public JobProgress getProgress(long jobId) {
		// TODO
		return null;
	}

	@Override
	public List<Long> getRunningJobIds() {
		Job currentJob = this.currentJob;
		return currentJob == null ? Collections.emptyList() : Collections.singletonList(currentJob.getId());
	}
}
