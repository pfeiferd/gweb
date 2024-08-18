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
package org.metagene.gweb.service;

import java.util.Comparator;

import org.metagene.gweb.service.dto.DTO;

public interface CRUDService<D extends DTO> extends Service {
	public static final Comparator<DTO> ID_DTO_COMPARATOR = new Comparator<DTO>() {
		@Override
		public int compare(DTO o1, DTO o2) {
			long c = o1.getId() - o2.getId();
			return c < 0 ? -1 : (c == 0 ? 0 : 1);
		}
	};

	public long create(D d);
	public void remove(long id);
	public D get(long id);
	
	// Result ordered by ids.
	public D[] getAll();
	public void update(D d);
}
