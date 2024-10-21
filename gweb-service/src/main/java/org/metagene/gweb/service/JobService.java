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

import java.io.File;

import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Job.JobType;
import org.metagene.gweb.service.dto.JobProgress;

public interface JobService extends CRUDService<Job> {
	default boolean compareStatusForJob(Job job, JobStatus... stati) {
		for (JobStatus status : stati) {
			if (status.equals(job.getStatus())) {
				return true;
			}
		}
		return false;
	}

	default void assertStatusForJob(Job job, JobStatus... stati) {
		if (!compareStatusForJob(job, stati)) {
			throw new ValidationException("Cannot update job in status " + job.getStatus());
		}
	}

	default void assertTypeForJob(Job job, JobType... types) {
		for (JobType type : types) {
			if (type.equals(job.getJobType())) {
				return;
			}
		}
		throw new ValidationException("Cannot create job with type " + job.getJobType());
	}

	public JobStatus enqueue(long jobId);

	public long enqueueDBInfo(long dbId, long userId);

	public long enqueueDBInstall(long dbId, long userId);
	
	public JobStatus cancel(long jobId);

	// Result ordered by ids.
	public Job[] getByUser(long userId);

	public JobProgress getProgress(long jobId);

	public JobStatus[] getStatusForJobs(long[] jobIds);

	public long[] getActiveJobIds();
	
	public long[] getJobIdsByStatus(JobStatus status);
	
	public boolean isLogExists(long jobId);
	public boolean isCSVExists(long jobId);
	
	public File getLogFile(long jobId);
	public File getCSVFile(long jobId);
}
