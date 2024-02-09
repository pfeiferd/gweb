package org.metagene.genestrip.service.dto;

import java.io.Serializable;

public class JobProgress implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final long coveredBytes;
	private final long totalBytes;
	private final long elapsedTimeMs;
	private final long totalTimeMs;

	public JobProgress(long coveredBytes, long totalBytes, long elapsedTimeMs, long totalTimeMs) {
		this.coveredBytes = coveredBytes;
		this.totalBytes = totalBytes;
		this.elapsedTimeMs = elapsedTimeMs;
		this.totalTimeMs = totalTimeMs;
	}

	public long getCoveredBytes() {
		return coveredBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public long getElapsedTimeMs() {
		return elapsedTimeMs;
	}

	public long getTotalTimeMs() {
		return totalTimeMs;
	}
}
