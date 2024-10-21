package org.metagene.gweb.service.genestrip.upload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.metagene.genestrip.io.StreamingResource;
import org.metagene.genestrip.io.StreamingResourceListStream;
import org.metagene.genestrip.io.StreamingResourceStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@MultipartConfig
public class FastqUploadProcessor extends AbstractFastqUploadProcessor {
	private static final long serialVersionUID = 1L;

	@Override
	protected StreamingResourceStream createStreamingResourceStream(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!initJobId(request.getParameter(JOBID_PARAM), response)) {
			return null;
		}

		List<StreamingResource> list = new ArrayList<StreamingResource>();
		for (Part part : request.getParts()) {
			if (FILE_PARAM.equals(part.getName())) {
				list.add(new PartStreamingResource(part));
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong field name in multipart stream");
				return null;
			}
			if (part.getSubmittedFileName() == null || part.getSubmittedFileName().isBlank()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"blank or missing file name in multipart stream");
				return null;
			}
			boolean suffixMatch = false;
			for (String suffix : FILE_NAME_SUFFIXES) {
				if (part.getSubmittedFileName().endsWith(suffix)) {
					suffixMatch = true;
					break;
				}
			}
			if (!suffixMatch) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "wrong file name suffix in multipart stream");
				return null;
			}
		}
		if (list.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing param fastq");
			return null;
		}
		return new StreamingResourceListStream(list);
	}
}
