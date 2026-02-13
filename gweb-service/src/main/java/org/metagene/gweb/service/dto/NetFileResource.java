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
import java.nio.file.FileSystems;

public class NetFileResource extends DTO {
	public static int NAME_SIZE = 256;
	public static int URL_SIZE = 1024;

	private static final long serialVersionUID = 1L;

	public enum ResourceType {
		HTTP_URL, FILE_PATH;

		private static final ResourceType[] RESOURCE_VALUES = ResourceType.values();

		public static ResourceType indexToValue(int index) {
			if (index >= 0 && index < RESOURCE_VALUES.length) {
				return RESOURCE_VALUES[index];
			}
			return null;
		}
	}

	private String name;
	private ResourceType type;
	private long userId;
	private String url;

	public NetFileResource() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean checkNameValid() {
		return name != null && !name.isEmpty();
	}

	public ResourceType getType() {
		return type;
	}

	public void setType(ResourceType type) {
		this.type = type;
	}

	public boolean checkTypeValid() {
		return type != null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean checkURLValid() {
		switch (type) {
		case HTTP_URL:
			return checkHTTPURLValid(url) && checkURLSafe(url);
			// TODO: This is very insecure. Best forbid 'FILE_PATH' for non-local deployment altogether...
		case FILE_PATH:
			return checkGLOBFilePath(url);
		default:
			return false;
		}
	}

	public boolean checkGLOBFilePath(String s) {
		try {
			File parent = new File(s).getParentFile();
			if (parent != null) {
				parent.toPath();
			}
			FileSystems.getDefault().getPathMatcher("glob:" + s);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean checkUserIdValid() {
		return isValidId(userId);
	}

	@Override
	public boolean checkValid() {
		return super.checkValid() && checkNameValid() && checkTypeValid() && checkURLValid() && checkUserIdValid();
	}
}
