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

import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.dto.NetFileResource;
import org.metagene.gweb.service.dto.NetFileResource.ResourceType;
import org.metagene.gweb.service.dto.User.UserRole;

public class ResourceRoleService extends CRUDRoleService<ResourceService, NetFileResource> implements ResourceService {
	private final UserRole minFilePathUserRole;

	public ResourceRoleService(ResourceService delegate, UserStore userStore, UserRole minFilePathUserRole) {
		super(delegate, userStore);
		this.minFilePathUserRole = minFilePathUserRole;
	}

	@Override
	public long create(NetFileResource d) {
		checkJobsAllowed();
		checkIsMyResource(d);

		return getDelegate().create(d);
	}

	@Override
	public void remove(long id) {
		checkJobsAllowed();
		checkIsMyResource(id);
		getDelegate().remove(id);
	}

	@Override
	public NetFileResource get(long id) {
		checkJobsAllowed();
		checkIsMyResource(id);
		return getDelegate().get(id);
	}

	@Override
	public void update(NetFileResource d) {
		checkJobsAllowed();
		checkIsMyResource(d.getId());
		if (ResourceType.FILE_PATH.equals(d.getType())) {
			if (minFilePathUserRole == null || !getLoggedInUserRole().subsumes(minFilePathUserRole)) {
				throw new MissingRightException("Access right for resource type missing in role.");
			}
		}
		getDelegate().update(d);
	}

	@Override
	public NetFileResource[] getByUser(long userId) {
		checkReadAllowed();
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			if (userId != getLoggedInUser().getId()) {
				throw new MissingRightException("Resource access right for resources missing in role.");
			}
		}
		return getDelegate().getByUser(userId);
	}

	protected void checkIsMyResource(long id) {
		if (getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			return;
		} else {
			NetFileResource res = getDelegate().get(id);
			if (res != null) {
				checkIsMyResource(res);
			}
		}
	}

}
