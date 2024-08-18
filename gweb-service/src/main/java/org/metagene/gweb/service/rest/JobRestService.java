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
package org.metagene.gweb.service.rest;

import java.io.File;

import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.JobProgress;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/JobService")
public abstract class JobRestService extends CRUDRestService<JobService, Job> implements JobService {
	@Override
	@GET
	@Path("enqueue/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public JobStatus enqueue(@PathParam("id") long jobId) {
		return getDelegate().enqueue(jobId);
	}

	@Override
	@GET
	@Path("enqueueDBInfo/{id}/{uid}")
	@Produces(APPLICATION_JSON_UTF8)
	public long enqueueDBInfo(@PathParam("id") long dbId, @PathParam("uid") long userId) {
		return getDelegate().enqueueDBInfo(dbId, userId);
	}

	@Override
	@GET
	@Path("enqueueDBInstall/{id}/{uid}")
	@Produces(APPLICATION_JSON_UTF8)
	public long enqueueDBInstall(@PathParam("id") long dbId, @PathParam("uid") long userId) {
		return getDelegate().enqueueDBInstall(dbId, userId);
	}

	@Override
	@GET
	@Path("cancel/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public JobStatus cancel(@PathParam("id") long jobId) {
		return getDelegate().cancel(jobId);
	}

	@Override
	@GET
	@Path("getByUser/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public Job[] getByUser(@PathParam("id") long userId) {
		return getDelegate().getByUser(userId);
	}

	@Override
	@GET
	@Path("getProgress/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	public JobProgress getProgress(@PathParam("id") long jobId) {
		return getDelegate().getProgress(jobId);
	}

	@Override
	@POST
	@Path("getStatusForJobs")
	@Produces(APPLICATION_JSON_UTF8)
	@Consumes(MediaType.APPLICATION_JSON)
	public JobStatus[] getStatusForJobs(long[] jobIds) {
		return getDelegate().getStatusForJobs(jobIds);
	}

	@Override
	@GET
	@Path("getJobIdsByStatus/{status}")
	@Produces(APPLICATION_JSON_UTF8)
	public long[] getJobIdsByStatus(@PathParam("status") JobStatus status) {
		return getDelegate().getJobIdsByStatus(status);
	}
	@GET
	@Path("isCSVExists/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	@Override
	public boolean isCSVExists(@PathParam("id") long jobId) {
		return getDelegate().isCSVExists(jobId);
	}

	@GET
	@Path("isLogExists/{id}")
	@Produces(APPLICATION_JSON_UTF8)
	@Override
	public boolean isLogExists(@PathParam("id") long jobId) {
		return getDelegate().isLogExists(jobId);
	}
		
	@GET
	@Path("getLog/{id}")
	@Produces(TEXT_PLAIN_UTF8)
	public Response getLog(@PathParam("id") long jobId) {
		return sendFile(getLogFile(jobId));
	}
	
	@Override
	public File getLogFile(long jobId) {
		return getDelegate().getLogFile(jobId);
	}

	@GET
	@Path("getCSV/{id}")
	@Produces(TEXT_PLAIN_UTF8)
	public Response getCSV(@PathParam("id") long jobId) {
		return sendFile(getCSVFile(jobId));
	}
	
	@Override
	public File getCSVFile(long jobId) {
		return getDelegate().getCSVFile(jobId);
	}
}
