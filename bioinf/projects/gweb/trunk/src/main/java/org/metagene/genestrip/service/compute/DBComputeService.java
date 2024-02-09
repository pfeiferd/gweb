package org.metagene.genestrip.service.compute;

import java.util.List;

import org.metagene.genestrip.service.DBService;
import org.metagene.genestrip.service.dto.DB;
import org.metagene.genestrip.service.dto.DBInfo;

public class DBComputeService implements DBService {
	private final DBService delegate;

	public DBComputeService(DBService delegate) {
		this.delegate = delegate;
	}

	@Override
	public long create(DB d) {
		return delegate.create(d);
	}

	@Override
	public boolean remove(long id) {
		return delegate.remove(id);
	}

	@Override
	public DB get(long id) {
		return delegate.get(id);
	}

	@Override
	public List<DB> getAll() {
		return delegate.getAll();
	}

	@Override
	public boolean update(DB d) {
		return delegate.update(d);
	}
	
	@Override
	public DBInfo getDBInfo(long dbID) {
		// TODO Auto-generated method stub
		return null;
	}
}
