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

public class DB extends DTO {
	public static int NAME_SIZE = 256;
	public static int DB_FILE_PREFIX_SIZE = 256;
	public static int URL_SIZE = NetFileResource.URL_SIZE;
	public static int MD5_SIZE = 32;

	private static final long serialVersionUID = 1L;

	private String name;
	private String dbFilePrefix;
	private String installURL;
	private String installMD5;
	private boolean installed;
	private boolean infoExists;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean checkNameValid() {
		return name != null && !name.isEmpty();
	}

	public String getDbFilePrefix() {
		return dbFilePrefix;
	}

	public void setDbFilePrefix(String dbFilePrefix) {
		this.dbFilePrefix = dbFilePrefix;
	}

	public boolean checkDbFilePrefixValid() {
		return dbFilePrefix != null && !dbFilePrefix.isEmpty();
	}

	public String getInstallURL() {
		return installURL;
	}

	public void setInstallURL(String installURL) {
		this.installURL = installURL;
	}

	public String getInstallMD5() {
		return installMD5;
	}

	public void setInstallMD5(String installMD5) {
		this.installMD5 = installMD5;
	}
	
	public boolean isInstalled() {
		return installed;
	}
	
	public void setInstalled(boolean installed) {
		this.installed = installed;
	}
	
	public boolean isInfoExists() {
		return infoExists;
	}
	
	public void setInfoExists(boolean infoExists) {
		this.infoExists = infoExists;
	}

	public boolean checkURLValid() {
		if (installURL != null && !installURL.isEmpty()) {
			checkURLValid(installURL);
		}
		return true;
	}

	public boolean checkMD5Valid() {
		return installMD5 == null || (installMD5.length() == 32 && isHexNumber(installMD5));
	}

	private static boolean isHexNumber(String s) {
		return s.matches("^[0-9a-fA-F]+$");
	}

	@Override
	public boolean checkValid() {
		return super.checkValid() && checkNameValid() && checkDbFilePrefixValid() && checkURLValid() && checkMD5Valid();
	}
}
