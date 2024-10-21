package org.metagene.gweb.service.genestrip.upload;

import java.io.IOException;
import java.util.Iterator;

import org.metagene.genestrip.io.StreamingResourceStream;
import org.metagene.gweb.service.io.StreamingMulitpartFormDataParser;
import org.metagene.gweb.service.io.StreamingMulitpartFormDataParser.DataEntry;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
public class StreamingFastqUploadProcessor extends AbstractFastqUploadProcessor {
	private static final long serialVersionUID = 1L;

	private final StreamingMulitpartFormDataParser parser = new StreamingMulitpartFormDataParser();

	@Override
	protected StreamingResourceStream createStreamingResourceStream(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		parser.setInputStream(request.getInputStream());
		Iterator<DataEntry> it = parser.getEntryIterartor();

		if (!initJobId(getFieldValue(it, JOBID_PARAM), response)) {
			return null;
		}
		
		String fileNames = getFieldValue(it, FILE_NAMES_PARAM);
		if (fileNames == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing param " + FILE_NAMES_PARAM);
			return null;
		}
		String[] names = fileNames.split("/");

		String sizesStr = getFieldValue(it, FILE_SIZES_PARAM);
		if (sizesStr == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing param " + FILE_SIZES_PARAM);
			return null;
		}
		String[] s = sizesStr.split(",");
		long[] fileSizes = new long[s.length];
		for (int i = 0; i < s.length; i++) {
			try {
				fileSizes[i] = Long.parseLong(s[i].trim());
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "bad param " + FILE_SIZES_PARAM);
				return null;
			}
		}

		return new StreamingResourceUploadStream(names, fileSizes, it);
	}

	protected String getFieldValue(Iterator<DataEntry> it, String name) throws IOException {
		if (it.hasNext()) {
			DataEntry item = it.next();
			if (!item.hasValue() || !name.equals(item.getFieldName())) {
				return null;
			}
			return item.getValue();
		}
		return null;
	}
}
