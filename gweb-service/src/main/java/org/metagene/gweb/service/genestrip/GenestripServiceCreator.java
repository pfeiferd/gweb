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
import java.io.IOException;

import javax.sql.DataSource;

import org.metagene.genestrip.GSCommon;
import org.metagene.genestrip.GSProject;
import org.metagene.gweb.service.compute.JobExecutable.Factory;
import org.metagene.gweb.service.create.ServiceCreator;

public class GenestripServiceCreator extends ServiceCreator {
	private GSCommon gsCommon;

	public GenestripServiceCreator(DataSource dataSource, Config config, Logger logger) {
		super(dataSource, config, logger);
	}
	
	@Override
	protected void createGSConfig(File genestripBaseDir) {
		try {
			gsCommon = new GSCommon(genestripBaseDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public File getFastqDir() {
		return gsCommon.getFastqDir();
	}
	
	@Override
	protected File getCSVBaseDir(String projectName) {
		return new GSProject(gsCommon, projectName, true).getResultsDir();
	}
	
	@Override
	protected File getLogBaseDir(String projectName) {
		return new GSProject(gsCommon, projectName, true).getLogDir();
	}

	@Override
	protected File getDBFile(String projectName) {
		return new GSProject(gsCommon, projectName, true).getDBFile();
	}
	
	@Override
	protected File getDBInfoFile(String projectName) {
		return new GSProject(gsCommon, projectName, true).getDBInfoFile();		
	}
	
	@Override
	protected Factory createsJobExecutableFactory() {
		int threads = Integer.parseInt(getConfig().getConfigValue(THREADS, -1));
		long logProgress = Long.parseLong(getConfig().getConfigValue(LOG_PROGRESS, 1000000));
		return new GenestripJobExecutableFactory(gsCommon, threads, logProgress);
	}
}
