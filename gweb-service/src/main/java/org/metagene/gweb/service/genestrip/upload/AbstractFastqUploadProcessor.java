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
package org.metagene.gweb.service.genestrip.upload;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.metagene.genestrip.io.StreamingResourceStream;
import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.ValidationException;
import org.metagene.gweb.service.create.ServiceCreator;
import org.metagene.gweb.service.dto.DTO;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Job.JobType;
import org.metagene.gweb.service.dto.User.UserRole;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.genestrip.StreamingResourceForJobProvider;
import org.metagene.gweb.service.rest.RestApplication;
import org.metagene.gweb.service.rest.RestService;
import org.metagene.gweb.service.role.JobRoleService;
import org.metagene.gweb.service.role.MissingRightException;
import org.metagene.gweb.service.role.UserStore;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public abstract class AbstractFastqUploadProcessor extends HttpServlet implements StreamingResourceForJobProvider {
	public static final String JOBID_PARAM = "jobid";
	public static final String FILE_SIZES_PARAM = "filesizes";
	public static final String FILE_NAMES_PARAM = "filenames";
	public static final String FILE_PARAM = "fastq";
	public static final List<String> FILE_NAME_SUFFIXES = Collections.unmodifiableList(
			Arrays.asList(new String[] { ".fq", ".fastq", ".gz", ".gzip"
					// The following would be better, but it cannot be enforced by the browser:					
					// ".fq", ".fastq", ".fq.gzip", ".fq.gz", ".fastq.gzip", ".fastq.gz" 
					// I decided to leave it to it - Genestrip will fail anyways if the fastq file is corrupt - so better give it a try first.
			}));

	private static final long serialVersionUID = 1L;

	private final Object waitObject = new Object();
	private final long waitMs = 10000;
	private final long pollWaitMs = 1000;

	private final Object syncObject = new Object();
	private final Object syncBusy = new Object();

	// Consistent access to these field is ensured by synchronized doPost()
	private StreamingResourceStream srs = null;
	private long jobId = DTO.INVALID_ID;
	private boolean busy = false;

	private JobService jobComputeService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		config.getServletContext().setAttribute(RestApplication.SR_FOR_JOB_PROVIDER, this);
	}

	public StreamingResourceStream getResourcesForJob(Job job) {
		return job.getId() == jobId ? srs : null;
	}

	public Object getJobStartSyncObject() {
		return syncObject;
	}

	private boolean isUploadAllowed(HttpServletRequest request) {
		String uploadRole = request.getServletContext().getInitParameter(ServiceCreator.UPLOAD_PATH_ROLE);
		if (uploadRole != null) {
			UserRole role = UserRole.valueOf(uploadRole);
			if (role != null) {
				User user = getUser(request);

				if (user != null && user.getRole().subsumes(role)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!isUploadAllowed(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "fastq upload not allowed for role");
			return;
		}

		if (request.getContentType() == null
				|| request.getContentType().toLowerCase().indexOf("multipart/form-data") == -1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "content type must be multipart/form-data");
			return;
		}

		// Only one servlet thread at a time can do this...
		synchronized (syncBusy) {
			if (busy) {
				response.sendError(HttpServletResponse.SC_CONFLICT, "busy with upload match job");
				return;
			}
			busy = true;
		}
		try {
			StreamingResourceStream stream = createStreamingResourceStream(request, response);
			if (stream == null) {
				return;
			}

			JobService jobService = initJobService(request);
			Job job = jobService.get(jobId);
			if (job == null || !JobStatus.CREATED.equals(job.getStatus())
					|| !JobType.UPLOAD_MATCH.equals(job.getJobType())) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "invalid job");
				return;
			}
			
			// That shall be enough for a rough check to avoid waiting.
			if (jobService.getActiveJobIds().length > 0) {
				response.sendError(HttpServletResponse.SC_CONFLICT, "busy with other match job");				
			}
			
			if (stream instanceof StreamingResourceUploadStream) {
				String[] fileNames = ((StreamingResourceUploadStream) stream).getFileNames();
				if (fileNames.length > 0) {
					job.setFastqFile(fileNames[0]);
					if (fileNames.length == 2) {
						job.setFastqFile2(fileNames[1]);
					}
					else if (fileNames.length > 2) {
						job.setFastqFile2(fileNames[1] + ", ...");
					}
					jobService.update(job);
				}				
			}

			srs = stream;
			// If worse comes to worst, this job will have to wait still as it is simply enqueued...
			jobService.enqueue(jobId);
			try {
				synchronized (waitObject) {
					// Wait for a while to see if job got picked up...
					waitObject.wait(waitMs);
				}
			} catch (InterruptedException e) {
				// Ignore on purpose.
			}
			JobStatus[] status = null;
			synchronized (syncObject) {
				status = jobService.getStatusForJobs(new long[] { jobId });
				if (JobStatus.ENQUEUED.equals(status[0])) {
					jobService.cancel(jobId);
					response.sendError(HttpServletResponse.SC_CONFLICT, "internal timeout for upload");
					return;
				}
			}
			// We must wait until the job is done to keep the request consistent...
			// ... as it contains all the fastq files (i.e. parts) to be analyzed.
			while (JobStatus.STARTED.equals(status[0])) {
				try {
					synchronized (waitObject) {
						// Wait for a while to see if job is done...
						waitObject.wait(pollWaitMs);
					}
				} catch (InterruptedException e) {
					// Ignore on purpose.
				}
				status = jobService.getStatusForJobs(new long[] { jobId });
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (MissingRightException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "cannot access job");
		} catch (ValidationException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "invalid job");
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"could not process request: " + e.getMessage());
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error: " + e.getMessage());
		} finally {
			// To prevent potential memory leaks from keeping a request's parts in the srs
			// field.
			jobId = DTO.INVALID_ID;
			srs = null;
			busy = false;
		}
	}

	protected abstract StreamingResourceStream createStreamingResourceStream(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException;

	protected JobService initJobService(HttpServletRequest request) {
		// From here on, no competing threads for this servlet
		if (jobComputeService == null) {
			jobComputeService = (JobService) request.getServletContext().getAttribute(RestApplication.JOB_SERVICE);
		}
		return new JobRoleService(jobComputeService, new UserStore() {
			@Override
			public void setUser(User u) {
				throw new UnsupportedOperationException("should never be called");
			}

			@Override
			public User getUser() {
				return AbstractFastqUploadProcessor.this.getUser(request);
			}
		}, null);
	}

	protected User getUser(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session == null ? null : (User) session.getAttribute(RestService.USER_ATTR);
	}

	protected boolean initJobId(String jobStr, HttpServletResponse response) throws IOException {
		if (jobStr == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing param " + JOBID_PARAM);
			return false;
		}
		try {
			this.jobId = Long.parseLong(jobStr.trim());
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad param " + JOBID_PARAM);
			return false;
		}
		return true;
	}
}
