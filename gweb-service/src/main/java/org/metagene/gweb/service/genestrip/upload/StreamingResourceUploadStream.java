package org.metagene.gweb.service.genestrip.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.metagene.genestrip.io.StreamProvider;
import org.metagene.genestrip.io.StreamingResource;
import org.metagene.genestrip.io.StreamingResourceStream;
import org.metagene.gweb.service.io.StreamingMulitpartFormDataParser.DataEntry;
import org.metagene.gweb.service.io.StreamingMulitpartFormDataParser.DataEntry.DataInputStream;

public class StreamingResourceUploadStream implements StreamingResourceStream {
	private final String[] fileNames;
	private final long[] byteSizes;
	private final long totalByteSize;
	private final Iterator<DataEntry> it;
	private boolean iterated;
	private int itCounter;

	public StreamingResourceUploadStream(String[] fileNames, long[] byteSizes, Iterator<DataEntry> it) {
		for (String fileName : fileNames) {
			boolean found = false;
			for (String suffix : getFileNameSuffixes()) {
				if (fileName.endsWith(suffix)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("bad file name suffix for file " + fileName);			
			}
		}
		if (fileNames.length != byteSizes.length) {
			throw new IllegalArgumentException("inconsistent number of file names and file sizes");
		}
		this.fileNames = fileNames;
		this.byteSizes = byteSizes;
		this.it = it;
		totalByteSize = getTotalByteSize(byteSizes);
		iterated = false;
		itCounter = 0;
	}

	@Override
	public int size() {
		return byteSizes.length;
	}

	@Override
	public long getTotalByteSize() throws IOException {
		return totalByteSize;
	}
	
	public String[] getFileNames() {
		return fileNames;
	}

	@Override
	public Iterator<StreamingResource> iterator() {
		if (iterated) {
			throw new IllegalStateException("iterator may only be called once");
		}
		iterated = true;
		return new Iterator<StreamingResource>() {
			@Override
			public boolean hasNext() {
				return itCounter < size() && it.hasNext();
			}

			@Override
			public StreamingResource next() {
				if (itCounter >= size()) {
					throw new IllegalStateException("iterator size exceeded");
				}
				DataEntry fi = it.next();
				if (fi.hasValue()) {
					throw new IllegalArgumentException("no form field expected in multipart stream");
				}
				if (!getFieldName().equals(fi.getFieldName())) {
					throw new IllegalArgumentException("wrong field name in multipart stream");
				}
				if (fi.getFileName() == null || fi.getFileName().isBlank()) {
					throw new IllegalArgumentException("blank or missing file name in multipart stream");					
				}
				if (!fileNames[itCounter].equals(fi.getFileName())) {
					throw new IllegalArgumentException("inconsistent file name " + fi.getFileName());
				}
				itCounter++;
				return new StreamingResource() {
					@Override
					public long getSize() throws IOException {
						return byteSizes[itCounter];
					}

					@Override
					public StreamAccess openStream() throws IOException {
						return new StreamAccess() {
							private DataInputStream stream;
							private InputStream is;

							public InputStream getInputStream() throws IOException {
								if (stream == null) {
									stream = (DataInputStream) fi.getInputStream();
									String name = fi.getFileName();
									if (name.endsWith(".gz") || name.endsWith(".gzip")) {
										is = new GZIPInputStream(stream, StreamProvider.getBufferSize());
									} else {
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

					@Override
					public String getName() {
						return fi.getFileName();
					}
					
					@Override
					public String toString() {
						String s = getName();
						if (s == null) {
							s = super.toString();
						}
						return "upload: " + s;
					}
				};
			}
		};
	}

	protected String getFieldName() {
		return AbstractFastqUploadProcessor.FILE_PARAM;
	}
	
	protected List<String> getFileNameSuffixes() {
		return AbstractFastqUploadProcessor.FILE_NAME_SUFFIXES;
	}

	private long getTotalByteSize(long[] byteSizes) {
		long sum = 0;
		for (long size : byteSizes) {
			if (size < 0) {
				return -1;
			}
			sum += size;
		}
		return sum;
	}
}
