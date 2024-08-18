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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metagene.gweb.service.FastqFileService;
import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.dto.User;

public class FastqFileComputeService implements FastqFileService {
	private static String[] fastqSuffix = new String[] { ".fastq", ".fq", ".fastq.gz", ".fq.gz", ".fastq.gzip",
			".fq.gzip" };

	private final File fastqBaseDir;
	private final UserService userService;
	private final FilenameFilter fastqFileFilter;

	public FastqFileComputeService(File fastqBaseDir, UserService userService) {
		this.fastqBaseDir = fastqBaseDir;
		this.userService = userService;

		if (!fastqBaseDir.exists()) {
			fastqBaseDir.mkdir();
		}
		fastqFileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				for (String suffix : fastqSuffix) {
					if (name.endsWith(suffix)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	@Override
	public String[] getFastqFilesForUser(long userId) {
		List<String> res = new ArrayList<String>();

		File folder = getFastqFolder(userId);
		if (folder != null && folder.exists()) {
			File[] files = folder.listFiles(fastqFileFilter);
			for (File file : files) {
				res.add(file.getName());
			}
		}

		return res.toArray(new String[res.size()]);
	}

	@Override
	public String getFastqFolderForUser(long userId) {
		File folder = getFastqFolder(userId);
		return folder != null ? folder.toString() : null;
	}

	protected File getFastqFolder(long userId) {
		User user = userService.get(userId);
		if (user != null) {
			String userFolder = Long.toString(userId);
			File file = new File(fastqBaseDir, userFolder);
			if (!file.exists()) {
				file.mkdir();
			}			
			try {
				return file.getCanonicalFile();
			} catch (IOException e) {
				return file.getAbsoluteFile();
			}
		}
		return null;
	}
}
