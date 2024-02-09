package org.metagene.genestrip.service.role;

import java.util.List;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.DBInfo;

public class DBRoleService extends CRUDRoleService<DBService, DB> implements DBService {
	public DBRoleService(DBService delegate, UserStore userStore) {
		super(delegate, userStore);
	}
	
	@Override
	public DB get(long id) {
		checkReadAllowed();
		return getDelegate().get(id);
	}
	
	@Override
	public List<DB> getAll() {
		checkReadAllowed();
		return getDelegate().getAll();
	}

	@Override
	public DBInfo getDBInfo(long dbID) {
		checkReadAllowed();
		return getDelegate().getDBInfo(dbID);
	}
}
