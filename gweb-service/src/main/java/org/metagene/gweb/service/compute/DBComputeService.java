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
package org.metagene.gweb.service.compute;

import java.io.File;

import org.metagene.gweb.service.DBService;
import org.metagene.gweb.service.dto.DB;

public abstract class DBComputeService implements DBService {
	private final DBService delegate;

	public DBComputeService(DBService delegate) {
		this.delegate = delegate;
	}

	@Override
	public long create(DB d) {
		return delegate.create(d);
	}

	@Override
	public void remove(long id) {
		delegate.remove(id);
	}

	@Override
	public DB get(long id) {
		DB res = delegate.get(id);
		if (res != null) {
			res.setInstalled(isDBInstalled(res));
			res.setInfoExists(isDBInfoExists(res));
		}
		return res;
	}

	@Override
	public DB[] getAll() {
		DB[] res = delegate.getAll();
		for (int i = 0; i < res.length; i++) {
			res[i].setInstalled(isDBInstalled(res[i]));
			res[i].setInfoExists(isDBInfoExists(res[i]));
		}
		return res;
	}

	@Override
	public void update(DB d) {
		delegate.update(d);
	}

	private boolean isDBInstalled(DB db) {
		File file = getDBFile(db.getDbFilePrefix());
		return file != null && file.exists();
	}

	private boolean isDBInfoExists(DB db) {
		File f = getDBInfoFile(db.getDbFilePrefix());
		return f != null && f.exists();
	}
	
	@Override
	public File getInfoFile(long dbId) {
		DB db = get(dbId);
		if (db != null) {
			return getDBInfoFile(db.getDbFilePrefix());
		}
		return null;
	}
	
	protected abstract File getDBFile(String projectName);
	protected abstract File getDBInfoFile(String projectName);
}
