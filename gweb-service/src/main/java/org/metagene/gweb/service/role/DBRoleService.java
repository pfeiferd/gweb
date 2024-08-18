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

import java.io.File;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.dto.DB;

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
	public DB[] getAll() {
		checkReadAllowed();
		return getDelegate().getAll();
	}

	@Override
	public File getInfoFile(long dbId) {
		checkReadAllowed();
		return getDelegate().getInfoFile(dbId);
	}
}
