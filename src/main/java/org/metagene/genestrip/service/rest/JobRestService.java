package org.metagene.genestrip.service.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.dto.JobProgress;
import org.metagene.genestrip.service.dto.JobResult;

@Path("/JobService")
public abstract class JobRestService extends CRUDRestService<JobService, Job> implements JobService {
	@Override
	@GET
	@Path("start/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JobStatus enqueue(long jobId) {
		return getDelegate().enqueue(jobId);
	}

	@Override
	@GET
	@Path("cancel/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JobStatus cancel(long jobId) {
		return getDelegate().cancel(jobId);
	}

	@Override
	@GET
	@Path("getByOwner/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Job> getByUser(long userId) {
		return getDelegate().getByUser(userId);
	}

	@Override
	@GET
	@Path("getResult/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JobResult getResult(long jobId) {
		return getDelegate().getResult(jobId);
	}

	@Override
	@GET
	@Path("getProgress/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JobProgress getProgress(long jobId) {
		return getProgress(jobId);
	}
	
	@Override
	@GET
	@Path("getPendingJobIds")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Long> getPendingJobIds() {
		return getDelegate().getPendingJobIds();
	}
	
	@Override
	@GET
	@Path("getRunningJobIds")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Long> getRunningJobIds() {
		return getDelegate().getRunningJobIds();
	}
}
