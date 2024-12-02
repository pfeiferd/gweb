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

import org.metagene.gweb.service.DefaultLoginService;
import org.metagene.gweb.service.FastqFileService;
import org.metagene.gweb.service.dto.User.UserRole;

public class FastqFileRoleService extends RoleService<FastqFileService> implements FastqFileService {
	private final boolean localInstall;

	public FastqFileRoleService(FastqFileService delegate, UserStore userStore, boolean localInstall, DefaultLoginService defaultLoginService) {
		super(delegate, userStore, defaultLoginService);
		this.localInstall = localInstall;
	}

	@Override
	public String[] getFastqFilesForUser(long userId) {
		checkJobsAllowed();
		checkAccessForUser(userId);
		return getDelegate().getFastqFilesForUser(userId);
	}

	@Override
	public String getFastqFolderForUser(long userId) {
		if (getLoggedInUserRole().subsumes(UserRole.ADMIN) || localInstall) {
			return getDelegate().getFastqFolderForUser(userId);
		}
		else {
			throw new MissingRightException("Read access right missing.");
		}
	}

	protected void checkAccessForUser(long userId) {
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN) && getLoggedInUser().getId() != userId) {
			throw new MissingRightException("Read access right missing.");
		}
	}
}
