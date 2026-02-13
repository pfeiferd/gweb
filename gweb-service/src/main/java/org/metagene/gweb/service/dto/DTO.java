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
import java.net.*;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.metagene.gweb.service.ValidationException;

@SuppressWarnings("serial")
@XmlRootElement
public abstract class DTO implements Serializable, Cloneable {
	public static long INVALID_ID = -1;

	public static boolean isValidId(long id) {
		return id >= 0;
	}

	private long id;

	public DTO() {
		id = INVALID_ID;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean checkIdValid() {
		return isValidId(id);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean checkValid() {
		return true;
	}

	public static boolean checkHTTPURLValid(String url) {
		try {
			URL u = new URL(url);
			return "http".equals(u.getProtocol()) || "https".equals(u.getProtocol());
		} catch (MalformedURLException e) {
			return false;
		}
	}

	public static boolean checkURLSafe(String url) {
		try {
			URI uri = new URI(url);
			String host = uri.getHost();

			// Resolve host to IP addresses
			InetAddress[] addresses = InetAddress.getAllByName(host);

			for (InetAddress address : addresses) {
				// Check for private IP ranges
				if (address.isSiteLocalAddress() ||
						address.isLoopbackAddress() ||
						address.isLinkLocalAddress() ||
						address.isAnyLocalAddress()) {
					return false;
				}

				// Check for RFC 1918 addresses
				byte[] ip = address.getAddress();
				if (ip.length == 4) {
					// Check for 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
					if ((ip[0] & 0xFF) == 10 ||
							((ip[0] & 0xFF) == 172 && (ip[1] & 0xFF) >= 16 && (ip[1] & 0xFF) <= 31) ||
							((ip[0] & 0xFF) == 192 && (ip[1] & 0xFF) == 168)) {
						return false;
					}
				}
			}
		} catch (URISyntaxException | UnknownHostException e) {
			return false;
		}
		return true;
	}
}
