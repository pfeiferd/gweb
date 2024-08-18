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
package org.metagene.gweb.launcher;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class Main {
	public Main(String baseDir, String warPath, int port) throws LifecycleException {
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(baseDir);
		tomcat.setPort(port);
		tomcat.enableNaming();
		File file = new File(warPath);
		tomcat.addWebapp("", file.getAbsolutePath());
		tomcat.getConnector().setURIEncoding("UTF-8");
		tomcat.start();
		
	}

	public static void main(String[] args) throws LifecycleException {
		String basePath = ".";
		String baseDir = basePath + "/tomcat";
		String warPath = basePath + "/lib/gweb.war";
		int port = 80;
		if (args.length > 0) {
			baseDir = args[0];
			if (args.length > 1) {
				warPath = args[1];
				if (args.length > 2) {
					String portStr = args[2];
					try {
						port = Integer.valueOf(portStr);
					} catch (NumberFormatException e) {						
						e.printStackTrace();
					}
				}
			}
		}
		new Main(baseDir, warPath, port);
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
				Desktop.getDesktop().browse(new URI("http://localhost:" + port));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}		
	}
}
