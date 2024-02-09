package org.metagene.genestrip.service;

import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.DBInfo;

public interface DBService extends CRUDService<DB> {
	public DBInfo getDBInfo(long dbID);
}
