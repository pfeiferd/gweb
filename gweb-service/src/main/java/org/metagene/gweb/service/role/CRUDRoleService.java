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
package org.metagene.gweb.service.role;

import org.metagene.gweb.service.CRUDService;
import org.metagene.gweb.service.DefaultLoginService;
import org.metagene.gweb.service.dto.DTO;

public class CRUDRoleService<S extends CRUDService<D>,D extends DTO> extends RoleService<S> implements CRUDService<D> {
	public CRUDRoleService(S delegate, UserStore userStore, DefaultLoginService defaultLoginService) {
		super(delegate, userStore, defaultLoginService);
	}

	@Override
	public long create(D d) {
		checkAllAllowed();
		return getDelegate().create(d);
	}

	@Override
	public void remove(long id) {
		checkAllAllowed();
		getDelegate().remove(id);
	}

	@Override
	public D get(long id) {
		checkAllAllowed();
		return getDelegate().get(id);
	}

	@Override
	public D[] getAll() {
		checkAllAllowed();
		return getDelegate().getAll();
	}

	@Override
	public void update(D d) {
		checkAllAllowed();
		getDelegate().update(d);
	}	
}
