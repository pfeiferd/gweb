package org.metagene.genestrip.service;

import java.util.List;

import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.dto.JobResult;
import org.metagene.genestrip.service.dto.JobProgress;

public interface JobService extends CRUDService<Job> {
	public JobStatus enqueue(long jobId);
	public JobStatus cancel(long jobId);
	public List<Job> getByUser(long userId);
	public List<Long> getPendingJobIds();
	public JobResult getResult(long jobId);
	public JobProgress getProgress(long jobId);
	public List<Long> getRunningJobIds();
}
