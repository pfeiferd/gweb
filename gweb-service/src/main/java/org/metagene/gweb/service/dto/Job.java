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
package org.metagene.gweb.service.dto;

import java.io.File;
import java.util.Date;

public class Job extends DTO {
	public static int NAME_SIZE = 255;
	public static int FASTQ_FILE_SIZE = 255;

	private static final long serialVersionUID = 1L;

	public enum JobStatus {
		CREATED, ENQUEUED, STARTED, FINISHED, E_CANCELED, S_CANCELED;

		private static final JobStatus[] JOB_STATUS_VALUES = JobStatus.values();

		public static JobStatus indexToValue(int index) {
			if (index >= 0 && index < JOB_STATUS_VALUES.length) {
				return JOB_STATUS_VALUES[index];
			}
			return null;
		}
	};

	public enum JobType {
		LOCAL_MATCH, DB_INFO, RES_MATCH, INSTALL_DB;

		private static final JobType[] JOB_TYPE_VALUES = JobType.values();

		public static JobType indexToValue(int index) {
			if (index >= 0 && index < JOB_TYPE_VALUES.length) {
				return JOB_TYPE_VALUES[index];
			}
			return null;
		}
	}

	private String name;
	private String fastqFile;
	private String fastqFile2;
	private long resourceId;
	private long resourceId2;
	private long dbId;
	private long userId;

	private JobType jobType;
	private JobStatus status;

	private Date enqueued;
	private Date started;
	private Date finished;

	private long coveredBytes;
	
	private boolean classifyReads;

	public Job() {
		jobType = JobType.LOCAL_MATCH;
		resourceId = DTO.INVALID_ID;
		resourceId2 = DTO.INVALID_ID;
	}

	public String getName() {
		return name;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public long getResourceId2() {
		return resourceId2;
	}

	public void setResourceId2(long resourceId2) {
		this.resourceId2 = resourceId2;
	}

	public void setFastqFile(String fastqFile) {
		this.fastqFile = fastqFile;
	}

	public void setFastqFile2(String fastqFile2) {
		this.fastqFile2 = fastqFile2;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setDbId(long dbId) {
		this.dbId = dbId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean checkNameValid() {
		return name != null && !name.isEmpty();
	}

	public JobType getJobType() {
		return jobType;
	}

	public JobStatus getStatus() {
		return status;
	}

	public boolean checkStatusValid() {
		return status != null;
	}

	public boolean checkJobType() {
		return jobType != null;
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

	public void setJobType(JobType jobType) {
		this.jobType = jobType;
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
	
	public boolean checkFastqFileValid() {
		return (!JobType.LOCAL_MATCH.equals(jobType) && fastqFile == null) || (JobType.LOCAL_MATCH.equals(jobType)
				&& fastqFile != null && !fastqFile.isEmpty() && fastqFile.indexOf(File.separatorChar) == -1);
	}

	public boolean checkResourceIdValid() {
		return (!JobType.RES_MATCH.equals(jobType) && resourceId == INVALID_ID)
				|| (JobType.RES_MATCH.equals(jobType) && resourceId != INVALID_ID);
	}

	public boolean checkResourceId2Valid() {
		return resourceId2 == INVALID_ID || JobType.RES_MATCH.equals(jobType);
	}

	public String getFastqFile2() {
		return fastqFile2;
	}

	public boolean checkFastqFile2Valid() {
		return fastqFile2 == null || (JobType.LOCAL_MATCH.equals(jobType) && !fastqFile2.isEmpty()
				&& fastqFile2.indexOf(File.separatorChar) == -1);
	}

	public long getDbId() {
		return dbId;
	}
	
	public void setClassifyReads(boolean classifyReads) {
		this.classifyReads = classifyReads;
	}
	
	public boolean isClassifyReads() {
		return classifyReads;
	}

	public boolean checkDbIdValid() {
		return isValidId(dbId);
	}

	public long getUserId() {
		return userId;
	}

	public long getCoveredBytes() {
		return coveredBytes;
	}

	public void setCoveredBytes(long coveredBytes) {
		this.coveredBytes = coveredBytes;
	}

	public boolean checkUserIdValid() {
		return isValidId(userId);
	}

	@Override
	public boolean checkValid() {
		return super.checkValid() && checkNameValid() && checkStatusValid() && checkDbIdValid() && checkUserIdValid()
				&& checkFastqFileValid() && checkFastqFile2Valid() && checkJobType() && checkResourceIdValid()
				&& checkResourceId2Valid();
	}
}
