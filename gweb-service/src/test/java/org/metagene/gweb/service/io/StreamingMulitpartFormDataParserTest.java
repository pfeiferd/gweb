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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Test;
import org.metagene.gweb.service.io.StreamingMulitpartFormDataParser.DataEntry;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class StreamingMulitpartFormDataParserTest {
	byte[] byte1 = "@FP200005993L1C029R04400746873\r\nCTAATTAACTTAACAGTTTAAGTAATTTAATTAACATAG\r\n+\r\nGGGGIDFHFGEFGFEHHEEFFGHGFHHGEHHGHGBGGIH\r\n"
			.getBytes();
	byte[] byte2 = "@FP200005993L1C003R03403967808\r\nTTTGACTTCGGCGAGGTCTTTTTCGGCGTAGGGTTTGGCGCGGCGGGCGAGGTCGTGCAGGAAGGTGAGGACTTGTTCGGGCGTGTCCGCCATTTTGGTTGCCAAAGACAGCTCGGCGTAGTTTTTGAAGCCGAGCAATTTGGCGGTTTG\r\n+\r\nGFFHGGFFGHGHHEFFEGGFFGGIHFFIHHGFFGFHGFIGHGHGIGFHGEHFCIGGIHGIGHIHEHGGGEFGHGHCFGHIGGGEIFGHFGHHCIIGG@FFFFGHFGHGHEDEGFFHFHEGEHGHGFGD@GFEG>GFHFGEEHAFFGDF2G\r\n"
			.getBytes();

	@Test
	public void testParser() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		StreamingMulitpartFormDataParser parser = new StreamingMulitpartFormDataParser(false);
		parser.setInputStream(new TestServletInputStream(classLoader.getResourceAsStream("TestMulitpartRequest.txt")));

		Iterator<DataEntry> it = parser.getEntryIterartor();

		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		DataEntry entry = it.next();
		assertEquals("jobid", entry.getFieldName());
		assertTrue(entry.hasValue());
		assertEquals("55", entry.getValue());
		assertNull(entry.getInputStream());
		assertNull(entry.getFileName());

		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		entry = it.next();
		assertEquals("filesizes", entry.getFieldName());
		assertTrue(entry.hasValue());
		assertEquals("113,335", entry.getValue());
		assertNull(entry.getInputStream());
		assertNull(entry.getFileName());

		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		entry = it.next();
		assertEquals("fastq", entry.getFieldName());
		assertFalse(entry.hasValue());
		assertNull(entry.getValue());
		assertEquals("problem2.fastq", entry.getFileName());
		InputStream is = entry.getInputStream();
		for (int i = 0; i < byte1.length; i++) {
			assertEquals(byte1[i], (byte) is.read());
		}
		assertEquals(-1, is.read());
		assertEquals(-1, is.read());

		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		entry = it.next();
		assertEquals("fastq", entry.getFieldName());
		assertFalse(entry.hasValue());
		assertNull(entry.getValue());
		assertEquals("problem.fastq", entry.getFileName());
		is = entry.getInputStream();
		for (int i = 0; i < byte2.length; i++) {
			assertEquals(byte2[i], (byte) is.read());
		}
		assertEquals(-1, is.read());
		assertEquals(-1, is.read());

		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
		assertNull(it.next());
	}

	public static class TestServletInputStream extends ServletInputStream {
		private final InputStream delegate;
		private boolean lf;

		public TestServletInputStream(InputStream delegate) {
			this.delegate = delegate;
			lf = false;
		}

		@Override
		public boolean isFinished() {
			return false;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
		}

		@Override
		public int read() throws IOException {
			if (lf) {
				lf = false;
				return '\n';
			}
			int c = delegate.read();
			if (c == '\n') {
				lf = true;
				return '\r';
			}
			else {
				return c;
			}
		}
	}
}
