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
package org.metagene.gweb.service.create;

import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.create.ServiceCreator.Logger;
import org.metagene.gweb.service.dto.DB;

public class StandardDBsInstaller {
	public static final String STD_DB_PROPS = "stdDBs.properties";

	public static final String ENTRIEY_KEY = "entries";

	public static final String NAME_KEY = "name";
	public static final String URL_KEY = "url";
	public static final String MD5_KEY = "md5";

	private final DBService dbService;
	private final Logger logger;

	public StandardDBsInstaller(DBService dbService, Logger logger) {
		this.dbService = dbService;
		this.logger = logger;
	}

	public void install() {
		Properties dataProperties = new Properties();
		try {
			dataProperties.load(getClass().getClassLoader().getResourceAsStream(STD_DB_PROPS));

			String entries = dataProperties.getProperty(ENTRIEY_KEY);
			if (entries != null) {
				StringTokenizer tokenizer = new StringTokenizer(entries, ",");
				while (tokenizer.hasMoreTokens()) {
					String entry = tokenizer.nextToken().trim();

					DB db = new DB();
					db.setDbFilePrefix(entry);

					String name = dataProperties.getProperty(getKey(entry, NAME_KEY));
					if (name != null) {
						db.setName(name.trim());
					}
					String url = dataProperties.getProperty(getKey(entry, URL_KEY));
					if (url != null) {
						db.setInstallURL(url);
					}
					String md5 = dataProperties.getProperty(getKey(entry, MD5_KEY));
					if (md5 != null) {
						db.setInstallMD5(md5);
					}
					if (db.checkValid()) {
						dbService.create(db);
					}
					else {
						logger.log("DB data for entry " + entry + " is invalid and ignored.");
					}
				}
			}
		} catch (IOException e) {
			logger.log("Could not load " + STD_DB_PROPS, e);
		}
	}

	protected String getKey(String entry, String key) {
		return entry + "." + key;
	}
}
