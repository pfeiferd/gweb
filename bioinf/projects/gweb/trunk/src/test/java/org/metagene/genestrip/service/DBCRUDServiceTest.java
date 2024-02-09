package org.metagene.genestrip.service;

import org.metagene.genestrip.service.dto.DB;

public class DBCRUDServiceTest extends AbstractCRUDServiceTest<DBService, DB> {
	@Override
	protected boolean dtosEqual(DB d1, DB d2) {
		return d1.getDbFilePrefix().equals(d2.getDbFilePrefix()) && d1.getName().equals(d2.getName());
	}
	
	@Override
	protected DB newDTO() {
		DB d = new DB();
		updateDTO(d);
		
		return d;
	}
	
	@Override
	protected void updateDTO(DB d) {
		d.setDbFilePrefix(nextString());
		d.setName(nextString());
	}

	@Override
	protected Class<DBService> getServiceClass() {
		return DBService.class;
	}
}
