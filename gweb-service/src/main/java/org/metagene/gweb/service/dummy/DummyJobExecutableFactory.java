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
package org.metagene.gweb.service.dummy;

import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.compute.JobExecutable;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.JobProgress;

public class DummyJobExecutableFactory implements JobExecutable.Factory {
	@Override
	public JobExecutable createExecutable(Job job, DB db, ResourceService resourceService) {
		return new DummyJobExecutable(job, db);
	}

	public class DummyJobExecutable implements JobExecutable {
		private final Job job;
		private final DB db;
		private final int max;
		private final long sleepTime;
		private final long bytesFactor;

		private Thread runnerThread;
		private boolean finished;
		private int progress;

		public DummyJobExecutable(Job job, DB db) {
			this.job = job;
			this.db = db;
			this.max = 10;
			this.sleepTime = 1000;
			this.bytesFactor = 1024 * 1024;
			finished = false;
		}

		@Override
		public void execute() {
			try {
				finished = false;
				runnerThread = Thread.currentThread();
				for (progress = 0; progress < 10; progress++) {
					Thread.sleep(sleepTime);
				}
				finished = true;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public long getCoveredBytes() {
			return max * bytesFactor;
		}

		@Override
		public boolean hasFinished() {
			return finished;
		}

		@Override
		public void cancel() {
			runnerThread.interrupt();
		}

		@Override
		public JobProgress getProgress() {
			return new JobProgress(progress * bytesFactor, max * bytesFactor, progress * sleepTime, max * sleepTime, ((double) progress) / max);
		}

		@Override
		public DB getDB() {
			return db;
		}

		@Override
		public Job getJob() {
			return job;
		}
	}
}