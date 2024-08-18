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

import java.io.Serializable;

public class JobProgress implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final long coveredBytes;
	private final long totalBytes;
	private final long elapsedTimeMs;
	private final long totalTimeMs;
	private final double progressRatio;

	public JobProgress(long coveredBytes, long totalBytes, long elapsedTimeMs, long totalTimeMs, double progressRatio) {
		this.coveredBytes = coveredBytes;
		this.totalBytes = totalBytes;
		this.elapsedTimeMs = elapsedTimeMs;
		this.totalTimeMs = totalTimeMs;
		this.progressRatio = progressRatio;
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
	
	public double getProgressRatio() {
		return progressRatio;
	}
}
