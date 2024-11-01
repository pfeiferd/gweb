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
package org.metagene.gweb.service.genestrip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.metagene.genestrip.DefaultExecutionContext;
import org.metagene.genestrip.ExecutionContext;
import org.metagene.genestrip.GSCommon;
import org.metagene.genestrip.GSConfigKey;
import org.metagene.genestrip.GSGoalKey;
import org.metagene.genestrip.GSMaker;
import org.metagene.genestrip.GSProject;
import org.metagene.genestrip.GSProject.FileType;
import org.metagene.genestrip.goals.DBDownloadGoal;
import org.metagene.genestrip.goals.LoadDBGoal;
import org.metagene.genestrip.io.StreamingResourceStream;
import org.metagene.genestrip.make.Goal;
import org.metagene.genestrip.store.Database;
import org.metagene.genestrip.util.GSLogFactory;
import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.compute.JobExecutable;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.DTO;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobType;
import org.metagene.gweb.service.dto.JobProgress;
import org.metagene.gweb.service.dto.NetFileResource;

public class GenestripJobExecutableFactory implements JobExecutable.Factory {
	public static final String LOGGER_NAME = "gweb job executor";

	private final MyDefaultExecutionContext bundle;
	private final GSCommon common;
	private final StreamingResourceForJobProvider provider;

	private Database storeCache;
	private File storeCacheFile;

	public GenestripJobExecutableFactory(GSCommon common, int consumers, long logUpdateCycle,
			StreamingResourceForJobProvider provider) {
		this.common = common;
		bundle = new MyDefaultExecutionContext(consumers, logUpdateCycle) {
			@Override
			public boolean isRequiresProgress() {
				return true;
			}
		};
		this.provider = provider;
	}

	@Override
	public JobExecutable createExecutable(Job job, DB db, ResourceService resourceService) {
		return new GenestripJobExecutable(job, db, resourceService);
	}

	@Override
	public Object getJobStartSyncObject() {
		return provider == null ? null : provider.getJobStartSyncObject();
	}

	public class GenestripJobExecutable implements JobExecutable {
		private final Job job;
		private final DB db;
		private final ResourceService resourceService;

		private boolean finished;
		private Goal<GSProject> goal;

		public GenestripJobExecutable(Job job, DB db, ResourceService resourceService) {
			this.job = job;
			this.db = db;
			this.resourceService = resourceService;
			finished = false;
		}

		private String getFullFastqFilePath(String fileName) {
			if (fileName != null) {
				if (fileName.indexOf(File.pathSeparatorChar) != -1) {
					throw new IllegalStateException("Illegal fastq file name: " + fileName);
				}
				File dir = new File(common.getFastqDir(), String.valueOf(job.getUserId()));
				File file = new File(dir, fileName);
				if (file.exists()) {
					return file.getPath();
				}
			}
			return null;
		}

		@Override
		public void execute() {
			String file1 = null;
			String file2 = null;
			if (JobType.LOCAL_MATCH.equals(job.getJobType())) {
				file1 = getFullFastqFilePath(job.getFastqFile());
				file2 = getFullFastqFilePath(job.getFastqFile2());
			} else if (JobType.RES_MATCH.equals(job.getJobType())) {
				if (DTO.isValidId(job.getResourceId())) {
					NetFileResource r1 = resourceService.get(job.getResourceId());
					if (r1 != null) {
						file1 = r1.getUrl();
						if (DTO.isValidId(job.getResourceId2())) {
							NetFileResource r2 = resourceService.get(job.getResourceId2());
							if (r2 != null) {
								file2 = r2.getUrl();
							}
						}
					}
				}
			}
			String[] files = null;
			if (file1 != null) {
				if (file2 != null) {
					files = new String[] { file1, file2 };
				} else {
					files = new String[] { file1 };
				}
			}
			GSGoalKey matchKey = job.isClassifyReads() ? GSGoalKey.MATCH : GSGoalKey.MATCHLR;
			String key = String.valueOf(job.getId());
			GSProject project = new GSProject(common, db.getDbFilePrefix(), key, files, true) {
				@Override
				protected String getOutputFilePrefix(String goal) {
					if (goal.equals(matchKey.getName())) {
						return "";
					}
					return super.getOutputFilePrefix(goal);
				}

				@Override
				protected String getOutputFileGoalPrefix(String goal, String key) {
					if (goal.equals(matchKey.getName())) {
						return key;
					}
					return super.getOutputFileGoalPrefix(goal, key);
				}
				
				@Override
				public String getExtraResourcesKey() {
					return key;
				}

				@Override
				public StreamingResourceStream getExtraResources() {
					return (JobType.UPLOAD_MATCH.equals(job.getJobType()) && provider != null)
							? provider.getResourcesForJob(job)
							: null;
				}
			};
			if (!project.getProjectsDir().exists()) {
				project.getProjectsDir().mkdir();
			}
			if (!project.getProjectDir().exists()) {
				project.getProjectDir().mkdir();
			}
			if (!project.getLogDir().exists()) {
				project.getLogDir().mkdir();
			}
			File logFile = new File(project.getLogDir(), job.getId() + FileType.LOG.getSuffix());
			try (OutputStream logOut = new FileOutputStream(logFile)) {
				GSLogFactory.getInstance().setLogOutForThread(new PrintStream(logOut));
				GSMaker maker = new GSMaker(project) {
					@Override
					protected ExecutionContext createExecutionContext(GSProject project) {
						return bundle;
					}
				};
				try {
					switch (job.getJobType()) {
					case LOCAL_MATCH:
					case RES_MATCH:
					case UPLOAD_MATCH:
						match(project, maker, matchKey);
						break;
					case DB_INFO:
						genDBInfo(project, maker);
						break;
					case INSTALL_DB:
						installDB(project, maker);
						break;
					default:
						throw new IllegalStateException("Bad job type " + job.getJobType());
					}
					if (!finished) {
						GSLogFactory.getLog(LOGGER_NAME).info("Job got canceled.");
					}
				} catch (Throwable t) {
					GSLogFactory.getLog(LOGGER_NAME).error("Error during goal", t);
					if (!finished) {
						GSLogFactory.getLog(LOGGER_NAME).info("Job got canceled.");
					}
					throw t;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private void match(GSProject project, GSMaker maker, GSGoalKey matchKey) {
			if (job.getErrorRate() >= 0) {
				project.initConfigParam(GSConfigKey.MAX_READ_TAX_ERROR_COUNT, job.getErrorRate());
			}
			fetchDBFromCacheIfPossible(project, maker);
			goal = maker.getGoal(matchKey);
			bundle.clearThrowableList();
			bundle.clearProgressInfo();
			goal.make();

			finished = true;
		}

		// We cache the last store, so that it does not have to be reloaded every
		// time...
		private void fetchDBFromCacheIfPossible(GSProject project, GSMaker maker) {
			LoadDBGoal loadDBGoal = (LoadDBGoal) maker.getGoal(GSGoalKey.LOAD_DB);
			if (storeCache != null && storeCacheFile != null) {
				if (project.getDBFile().equals(storeCacheFile) && !loadDBGoal.isMade()) {
					loadDBGoal.setDatabase(storeCache);
				}
			}
			// Ensure the reference to the DB is gone for GC:
			clearDBIfCached(null);
			// The get method will consume a lot of memory (it loads the DB):
			storeCache = loadDBGoal.get();
			storeCacheFile = project.getDBFile();
		}

		private void clearDBIfCached(GSProject project) {
			if (storeCache != null && storeCacheFile != null) {
				if (project == null && project.getDBFile().equals(storeCacheFile)) {
					storeCache = null;
					storeCacheFile = null;
					// Reclaim all the memory now.
					System.gc();
					// Second time sometimes does the real job...
					System.gc();
				}
			}
		}

		private void genDBInfo(GSProject project, GSMaker maker) {
			if (project.getDBInfoFile().exists()) {
				project.getDBInfoFile().delete();
			}
			fetchDBFromCacheIfPossible(project, maker);
			goal = maker.getGoal("dbinfo");
			goal.make();
			finished = true;
		}

		private void installDB(GSProject project, GSMaker maker) {
			try {
				URL url = new URL(db.getInstallURL());
				String md5 = db.getInstallMD5();
				if (project.getDBFile().exists()) {
					project.getDBFile().delete();
				}
				if (project.getDBInfoFile().exists()) {
					project.getDBInfoFile().delete();
				}
				clearDBIfCached(project);
				long startMillis = System.currentTimeMillis();
				DBDownloadGoal downloadGoal = new DBDownloadGoal(project, url, md5, bundle.getLogUpdateCycle(),
						maker.getGoal(GSGoalKey.SETUP)) {
					protected void log(long bytesCovered) {
						if (getLogger().isTraceEnabled() || bundle.isRequiresProgress()) {
							if (bundle.isRequiresProgress()) {
								bundle.setTotalProgress(bytesCovered, -1, System.currentTimeMillis() - startMillis, -1,
										-1, 0, 0, 0);
							}
							if (getLogger().isTraceEnabled()) {
								getLogger().info("Bytes read: " + bytesCovered);
							}
						}
					}
				};
				goal = downloadGoal;
				bundle.clearThrowableList();
				bundle.clearProgressInfo();
				goal.make();
				finished = !downloadGoal.isDumped();
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} finally {
				if (!finished) {
					if (project.getDBFile().exists()) {
						project.getDBFile().delete();
					}
				}
			}
		}

		@Override
		public boolean hasFinished() {
			return finished;
		}

		@Override
		public long getCoveredBytes() {
			return bundle.coveredBytes;
		}

		@Override
		public void cancel() {
			if (goal != null) {
				goal.dump();
			}
		}

		@Override
		public JobProgress getProgress() {
			switch (job.getJobType()) {
			case LOCAL_MATCH:
			case RES_MATCH:
			case UPLOAD_MATCH:
			case INSTALL_DB:
				return new JobProgress(bundle.coveredBytes, bundle.estTotalBytes, bundle.elapsedTimeMS,
						bundle.estTotalTimeMS, bundle.ratio);
			default:
				return new JobProgress(0, 0, 0, 0, finished ? 1 : 0);
			}
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

	protected static class MyDefaultExecutionContext extends DefaultExecutionContext {
		protected long coveredBytes;
		protected long estTotalBytes;
		protected long elapsedTimeMS;
		protected long estTotalTimeMS;
		protected double ratio;

		public MyDefaultExecutionContext(int consumers, long logUpdateCycle) {
			super(consumers, logUpdateCycle);
		}

		@Override
		public void setTotalProgress(long coveredBytes, long estTotalBytes, long elapsedTimeMS, long estTotalTimeMS,
				double ratio, long totalProcessedReads, int index, int totalCount) {
			this.coveredBytes = coveredBytes;
			this.estTotalBytes = estTotalBytes;
			this.elapsedTimeMS = elapsedTimeMS;
			this.estTotalTimeMS = estTotalTimeMS;
			this.ratio = ratio;
		}

		protected void clearProgressInfo() {
			this.coveredBytes = 0;
			this.estTotalBytes = 0;
			this.elapsedTimeMS = 0;
			this.estTotalTimeMS = 0;
			this.ratio = 0;
		}
	}
}