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
package org.metagene.gweb.service.compute;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Job.JobType;
import org.metagene.gweb.service.dto.JobProgress;

public abstract class JobComputeService implements JobService {
	private final Log logger = LogFactory.getLog("jobcomputeservice");

	private final TimerTask timerTask;
	private final Timer timer;
	private Thread timerThread;

	private final DBService dbService;
	private final JobService delegate;

	private JobExecutable currentExecutable;

	public JobComputeService(JobService delegate, DBService dbService,
			ResourceService resourceService, long delay, long period, JobExecutable.Factory executableFactory) {
		this.delegate = delegate;
		this.dbService = dbService;
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				timerThread = Thread.currentThread();
				Job currentJob = null;
				try {
					if (currentExecutable == null) {
						long[] pendingJobs = delegate.getJobIdsByStatus(JobStatus.ENQUEUED);

						if (pendingJobs.length > 0) {
							synchronized (timer) {
								pendingJobs = delegate.getJobIdsByStatus(JobStatus.ENQUEUED);
								if (pendingJobs.length > 0) {
									currentJob = delegate.get(pendingJobs[0]);
									currentJob.setStarted(new Date());
									currentJob.setStatus(JobStatus.STARTED);
									delegate.update(currentJob);
									DB db = dbService.get(currentJob.getDbId());
									currentExecutable = executableFactory.createExecutable(currentJob, db, resourceService);
								}
							}
							if (currentExecutable != null) {
								currentExecutable.execute();
								synchronized (timer) {
									if (currentExecutable.hasFinished()) {
										currentJob.setFinished(new Date());
										currentJob.setStatus(JobStatus.FINISHED);
										currentJob.setCoveredBytes(currentExecutable.getCoveredBytes());
										delegate.update(currentJob);
									}
								}
							}
						}
					}
				} catch (Throwable t) {
					if (currentJob != null) {
						JobComputeService.this.cancel(currentJob.getId());
					}
					// Never give up because of errors.. but log...
					logger.error("Error during job exection", t);
				} finally {
					synchronized (timer) {
						currentExecutable = null;
					}
				}
			}
		};
		timer.schedule(timerTask, delay, period);
	}

	@Override
	public long[] getJobIdsByStatus(JobStatus status) {
		return delegate.getJobIdsByStatus(status);
	}

	@Override
	public long create(Job d) {
		assertTypeForJob(d, JobType.LOCAL_MATCH, JobType.RES_MATCH);
		return delegate.create(d);
	}

	@Override
	public void remove(long id) {
		cancel(id);
		Job job = get(id);
		delegate.remove(id);
		if (job != null) {
			File file = getResultFile(job);
			if (file != null && file.exists()) {
				file.delete();
			}
			File log = getLogFile(job);
			if (log != null && log.exists()) {
				log.delete();
			}
		}
	}

	@Override
	public Job get(long id) {
		return delegate.get(id);
	}

	@Override
	public Job[] getAll() {
		return delegate.getAll();
	}

	@Override
	public void update(Job d) {
		assertStatusForJob(d, JobStatus.CREATED);
		assertTypeForJob(d, JobType.LOCAL_MATCH, JobType.RES_MATCH);
		delegate.update(d);
	}

	@Override
	public JobStatus enqueue(long jobId) {
		return delegate.enqueue(jobId);
	}
	
	@Override
	public long enqueueDBInfo(long dbId, long userId) {
		Job dbInfoJob = new Job();
		dbInfoJob.setJobType(JobType.DB_INFO);
		dbInfoJob.setDbId(dbId);
		dbInfoJob.setUserId(userId);
		dbInfoJob.setName("dbinfo");
		long id = delegate.create(dbInfoJob);
		dbInfoJob.setId(id);
		
		JobStatus status = enqueue(id);
		if (JobStatus.ENQUEUED.equals(status)) {
			return id;
		}
		return -1;
	}

	@Override
	public long enqueueDBInstall(long dbId, long userId) {
		Job dbInfoJob = new Job();
		dbInfoJob.setJobType(JobType.INSTALL_DB);
		dbInfoJob.setDbId(dbId);
		dbInfoJob.setUserId(userId);
		dbInfoJob.setName("dbinstall");
		long id = delegate.create(dbInfoJob);
		dbInfoJob.setId(id);
		
		JobStatus status = enqueue(id);
		if (JobStatus.ENQUEUED.equals(status)) {
			return id;
		}
		return -1;
	}

	@Override
	public JobStatus cancel(long jobId) {
		synchronized (timer) {
			if (currentExecutable != null && jobId == currentExecutable.getJob().getId()) {
				if (currentExecutable.hasFinished()) {
					return JobStatus.FINISHED;
				}
				currentExecutable.cancel();
				if (timerThread != null) {
					timerThread.interrupt();
				}
			}
			return delegate.cancel(jobId);
		}
	}

	@Override
	public Job[] getByUser(long userId) {
		return delegate.getByUser(userId);
	}

	@Override
	public JobProgress getProgress(long jobId) {
		if (currentExecutable != null && jobId == currentExecutable.getJob().getId()) {
			return currentExecutable.getProgress();
		}
		return null;
	}

	@Override
	public JobStatus[] getStatusForJobs(long[] jobIds) {
		return delegate.getStatusForJobs(jobIds);
	}

	@Override
	public boolean isCSVExists(long jobId) {
		return getCSVFile(jobId) != null;
	}

	@Override
	public boolean isLogExists(long jobId) {
		return getLogFile(jobId) != null;
	}
	
	@Override
	public File getCSVFile(long jobId) {
		Job job = get(jobId);
		if (job != null) {
			JobStatus s = job.getStatus();
			if (JobStatus.FINISHED.equals(s)) {
				File file = getResultFile(job);
				if (file != null && file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	@Override
	public File getLogFile(long jobId) {
		Job job = get(jobId);
		if (job != null) {
			JobStatus s = job.getStatus();
			if (JobStatus.STARTED.equals(s) || JobStatus.S_CANCELED.equals(s) || JobStatus.FINISHED.equals(s)) {
				File file = getLogFile(job);
				if (file != null && file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	public File getResultFile(Job job) {
		DB db = dbService.get(job.getDbId());
		if (db != null) {
			String fileName = job.getId() + ".csv";
			return new File(getCSVBaseDir(db.getDbFilePrefix()), fileName);
		}
		return null;
	}

	public File getLogFile(Job job) {
		DB db = dbService.get(job.getDbId());
		if (db != null) {
			String fileName = job.getId() + ".log";
			return new File(getLogBaseDir(db.getDbFilePrefix()), fileName);
		}
		return null;
	}
	
	protected abstract File getLogBaseDir(String projectName);
	protected abstract File getCSVBaseDir(String projectName);
}
