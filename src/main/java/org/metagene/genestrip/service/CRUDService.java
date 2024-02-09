package org.metagene.genestrip.service;

import java.util.List;

import org.metagene.genestrip.service.dto.DTO;

public interface CRUDService<D extends DTO> {
	public long create(D d);
	public boolean remove(long id);
	public D get(long id);
	public List<D> getAll();
	public boolean update(D d);
}
