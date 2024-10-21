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
