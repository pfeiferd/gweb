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
package org.metagene.gweb.service.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.ServletInputStream;

public class StreamingMulitpartFormDataParser {
	public static final int DEFAULT_BUFFER_SIZE = 4096;

	private static final byte[] HEADER = "Content-Disposition: form-data; name=\"".getBytes();
	private static final byte[] FILE_HEADER = "\"; filename=\"".getBytes();

	private final byte[] buffer;
	private final byte[] separatorLine;
	private int separatorLineSize;
	private int pos;
	private int readLength;
	private boolean hasNext;
	private boolean newIs;
	private ServletInputStream inputStream;
	private boolean eod;
	private boolean eof = false;

	public StreamingMulitpartFormDataParser() {
		this(true);
	}

	public StreamingMulitpartFormDataParser(boolean eatCR) {
		this(DEFAULT_BUFFER_SIZE, eatCR);
	}

	public StreamingMulitpartFormDataParser(int bufferSize, boolean eatCR) {
		if (bufferSize < 1024) {
			throw new IllegalArgumentException("buffer size must be >= 1024");
		}
		buffer = new byte[bufferSize];
		separatorLine = new byte[bufferSize];
		newIs = false;
		readLength = -1;
	}

	public void setInputStream(ServletInputStream inputStream) throws IOException {
		newIs = true;
		this.inputStream = inputStream;
		try {
			separatorLineSize = inputStream.readLine(separatorLine, 0, separatorLine.length);
			eod = false;
		} catch (IOException e) {
			throw new IteratorRuntimeIOException(e);
		}
		if (separatorLineSize == -1) {
			throw new EOFException("missing separator line in input stream");
		}
		if (separatorLineSize < 2 || separatorLine[0] != '-' || separatorLine[1] != '-'
				|| separatorLineSize == separatorLine.length) {
			throw new IllegalStateException("bad separator line");
		}
	}

	public Iterator<DataEntry> getEntryIterartor() {
		if (!newIs) {
			throw new IllegalStateException("can only be called once on set input stream");
		}
		newIs = false;
		eof = true;
		return new Iterator<DataEntry>() {
			@Override
			public boolean hasNext() {
				if (eod) {
					return false;
				}
				if (hasNext) {
					return true;
				}
				readLineFromBaseStream();
				hasNext = readLength != -1;
				return hasNext;
			}

			@Override
			public DataEntry next() {
				if (eod) {
					return null;
				}
				if (!eof) {
					throw new IllegalStateException("stream not ready");
				}
				hasNext = false;
				if (readLength == -1) {
					readLineFromBaseStream();
				}
				if (readLength == -1) {
					return null;
				}
				if (readLength == buffer.length) {
					throw new IllegalStateException("bad header line or buffer to small");
				}
				for (int i = 0; i < HEADER.length; i++) {
					if (buffer[i] != HEADER[i]) {
						throw new IllegalStateException("bad header line, bad header start");
					}
				}
				int start = HEADER.length;
				int len;
				for (len = 0; start + len < readLength; len++) {
					if (buffer[start + len] == '"') {
						break;
					}
				}
				if (start + len == readLength - 1) {
					throw new IllegalStateException("bad header line, missing quotation");
				}
				String fieldName = new String(buffer, start, len);
				String fileName = null;
				if (buffer[start + len + 2] != '\n' && buffer[start + len + 1] != '\r') {
					for (int i = 0; i < FILE_HEADER.length; i++) {
						if (buffer[start + len + i] != FILE_HEADER[i]) {
							throw new IllegalStateException("bad header line");
						}
					}
					start = start + len + FILE_HEADER.length;
					for (len = 0; start + len < readLength; len++) {
						if (buffer[start + len] == '"') {
							break;
						}
					}
					fileName = new String(buffer, start, len);
				}
				if (buffer[start + len + 2] != '\n' || buffer[start + len + 1] != '\r') {
					throw new IllegalStateException("bad header line");
				}
				if (fileName != null) {
					// We don't check the line's content for simplicity (it contains "Content-Type:
					// ..."
					readLineFromBaseStream();
				}
				readLineFromBaseStream();
				if (readLength != 2) {
					throw new IllegalStateException("missing newline line");
				}
				pos = 2;
				try {
					return new DataEntry(fieldName, fileName);
				} catch (IOException e) {
					throw new IteratorRuntimeIOException(e);
				}
			}
		};
	}

	private void readLineFromBaseStream() {
		try {
			pos = 0;
			readLength = inputStream.readLine(buffer, 0, buffer.length);
		} catch (IOException e) {
			throw new IteratorRuntimeIOException(e);
		}
	}

	public class DataEntry {
		private final String fieldName;
		private final String fileName;
		private final DataInputStream inputStream;
		private final String value;
		private boolean almost;

		public DataEntry(String fieldName, String fileName) throws IOException {
			this.almost = false;
			this.fieldName = fieldName;
			this.fileName = fileName;
			eof = false;
			DataInputStream is = new DataInputStream() {
	            private volatile boolean closed;
				
	            // I found out by long trial and error and fixing "phantom bugs":
	            // This methods must not return 0 - as the default implementation of InputStream suggest and offers.
	            // Otherwise GZIPInputStream does not work. It seems to rely on available() > 0 for proper operation.
				@Override
				public int available() throws IOException {
					return readLength == -1 ? 0 : readLength - pos + 1;
				}
								
				@Override
				public void close() throws IOException {
					closed = true;
					super.close();
				}

				@Override
				public int read() throws IOException {
					if (closed) {
						throw new IOException("Stream closed");
					}
					if (eof) {
						return -1;
					}
					byte res;
					if (almost) {
						almost = false;
						res = '\n';
					} else if (pos == readLength - 2 && buffer[pos] == '\r' && buffer[pos + 1] == '\n') {
						checkAndHandleEOF();
						if (eof) {
							return -1;
						}
						res = '\r';
					} else {
						if (pos == readLength) {
							readLineFromBaseStream();
							if (readLength == -1) {
								eof = true;
								return -1;
							} else if (readLength == 2 && buffer[0] == '\r' && buffer[1] == '\n') {
								checkAndHandleEOF();
								if (eof) {
									return -1;
								}
								res = '\r';
							} else {
								res = buffer[pos++];
							}
						} else {
							res = buffer[pos++];
						}
					}
					bytesRead++;
					return res & 0xff;
				}

				private void checkAndHandleEOF() throws EOFException {
					almost = true;
					readLineFromBaseStream();
					if (readLength == -1) {
						eof = true;
					} else if (readLength == separatorLineSize || readLength == separatorLineSize + 2) {
						eof = true;
						for (int i = 0; i < separatorLineSize - 2; i++) {
							if (buffer[i] != separatorLine[i]) {
								eof = false;
								break;
							}
						}
						if (eof) {
							if (readLength == separatorLineSize + 2 && buffer[readLength - 4] == '-'
									&& buffer[readLength - 3] == '-') {
								eod = true;
							}
							readLength = -1;
						}
					}
				}
			};
			if (fileName == null) {
				// Not very efficient but not important here...
				value = IOUtils.toString(is, StandardCharsets.UTF_8);
				inputStream = null;
			} else {
				value = null;
				inputStream = is;
			}
		}

		public String getFieldName() {
			return fieldName;
		}

		public String getValue() {
			return value;
		}

		public boolean hasValue() {
			return inputStream == null;
		}

		public String getFileName() {
			return fileName;
		}

		public DataInputStream getInputStream() {
			return inputStream;
		}

		public abstract class DataInputStream extends InputStream {
			protected long bytesRead;

			public long getBytesRead() {
				return bytesRead;
			}
		}
	}

	public static class IteratorRuntimeIOException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public IteratorRuntimeIOException(IOException e) {
			super(e);
		}
	}
}
