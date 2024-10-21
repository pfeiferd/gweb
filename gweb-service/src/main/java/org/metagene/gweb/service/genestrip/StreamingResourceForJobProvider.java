package org.metagene.gweb.service.genestrip;

import org.metagene.genestrip.io.StreamingResourceStream;
import org.metagene.gweb.service.dto.Job;

public interface StreamingResourceForJobProvider {
	public StreamingResourceStream getResourcesForJob(Job job);
	public Object getJobStartSyncObject();
}
