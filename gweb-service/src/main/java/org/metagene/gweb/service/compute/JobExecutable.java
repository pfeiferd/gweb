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

import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.JobProgress;

public interface JobExecutable {
	public interface Factory {
		public JobExecutable createExecutable(Job job, DB db, ResourceService resourceService);
	}
	
	// Synchronous execution in current thread.
	public void execute();
	
	public boolean hasFinished();
	
	public void cancel();
	public JobProgress getProgress();
	public long getCoveredBytes();
	public Job getJob();
	public DB getDB();
}
