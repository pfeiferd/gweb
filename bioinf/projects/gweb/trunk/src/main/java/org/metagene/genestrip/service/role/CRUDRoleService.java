package org.metagene.genestrip.service.role;

import java.util.List;

import org.metagene.genestrip.service.CRUDService;
import org.metagene.genestrip.service.dto.DTO;

public class CRUDRoleService<S extends CRUDService<D>,D extends DTO> extends RoleService<S> implements CRUDService<D> {
	public CRUDRoleService(S delegate, UserStore userStore) {
		super(delegate, userStore);
	}

	@Override
	public long create(D d) {
		checkAllAllowed();
		return getDelegate().create(d);
	}

	@Override
	public boolean remove(long id) {
		checkAllAllowed();
		return getDelegate().remove(id);
	}

	@Override
	public D get(long id) {
		checkAllAllowed();
		return getDelegate().get(id);
	}

	@Override
	public List<D> getAll() {
		checkAllAllowed();
		return getDelegate().getAll();
	}

	@Override
	public boolean update(D d) {
		checkAllAllowed();
		return getDelegate().update(d);
	}	
}
