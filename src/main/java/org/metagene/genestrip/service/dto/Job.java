package org.metagene.genestrip.service.dto;

import java.util.Date;

public class Job extends DTO {
	private static final long serialVersionUID = 1L;

	public enum JobStatus {
		CREATED, ENQUEUED, STARTED, FINISHED, CANCELED, UNKNOWN;

		private static final JobStatus[] JOB_STATUS_VALUES = JobStatus.values();

		public static JobStatus indexToValue(int index) {
			if (index >= 0 && index < JOB_STATUS_VALUES.length) {
				return JOB_STATUS_VALUES[index];
			} else {
				return JobStatus.UNKNOWN;
			}
		}
	};

	private String name;
	private final String fastqFile;
	private final String fastqFile2;
	private final long dbId;
	private final long userId;

	private JobStatus status;

	private Date enqueued;
	private Date started;
	private Date finished;

	public Job(String fastqFile, String fastqFile2,long dbId, long userId) {
		this.fastqFile = fastqFile;
		this.fastqFile2 = fastqFile2;
		this.dbId = dbId;
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JobStatus getStatus() {
		return status;
	}
	
	public void setEnqueued(Date enqueued) {
		this.enqueued = enqueued;
	}
	
	public Date getEnqueued() {
		return enqueued;
	}

	public void setStatus(JobStatus status) {
		this.status = status;
	}

	public Date getStarted() {
		return started;
	}

	public void setStarted(Date started) {
		this.started = started;
	}

	public Date getFinished() {
		return finished;
	}

	public void setFinished(Date finished) {
		this.finished = finished;
	}
	
	public String getFastqFile() {
		return fastqFile;
	}
	
	public String getFastqFile2() {
		return fastqFile2;
	}

	public long getDbId() {
		return dbId;
	}

	public long getUserId() {
		return userId;
	}
}
