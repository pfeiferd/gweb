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
package org.metagene.gweb.service.genestrip.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.metagene.genestrip.io.ByteCountingInputStream;
import org.metagene.genestrip.io.StreamProvider;
import org.metagene.genestrip.io.StreamingResource;

import jakarta.servlet.http.Part;

public class PartStreamingResource implements StreamingResource {
	private final Part part;

	public PartStreamingResource(Part part) {
		this.part = part;
	}
	
	public String getName() {
		return part.getSubmittedFileName();
	}

	public long getSize() throws IOException {
		return part.getSize();
	}

	public boolean isExists() {
		return part != null;
	}

	@Override
	public String toString() {
		return getName();
	}

	public StreamAccess openStream() throws IOException {
		return new StreamAccess() {
			private ByteCountingInputStream stream;
			private InputStream is;

			public long getSize() throws IOException {
				return part.getSize();
			}

			public InputStream getInputStream() throws IOException {
				if (stream == null) {
					stream = new ByteCountingInputStream(part.getInputStream());
					String name = part.getSubmittedFileName();
					if (name.endsWith(".gz") || name.endsWith(".gzip")) {
						is = new GZIPInputStream(stream, StreamProvider.getBufferSize());
					}
					else {
						is = stream;
					}
				}
				return is;
			}

			public long getBytesRead() {
				return stream == null ? 0 : stream.getBytesRead();
			}
		};
	}
}
