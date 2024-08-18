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

import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.NetFileResource;
import org.metagene.gweb.service.dto.User;
import org.metagene.gweb.service.dto.User.UserRole;

public class RoleService<S> {
	private final S delegate;
	private final UserStore userStore;
	
	public RoleService(S delegate, UserStore userStore) {
		this.delegate = delegate;
		this.userStore = userStore;
	}
	
	protected S getDelegate() {
		return delegate;
	}
	
	protected User getLoggedInUser() {
		return userStore.getUser();
	}
	
	protected UserRole getLoggedInUserRole() {
		User user = userStore.getUser();
		return user == null ? UserRole.NONE : user.getRole();
	}
	
	protected void setLoggedInUser(User user) {
		userStore.setUser(user);
	}
	
	protected void checkAllAllowed() {
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			throw new MissingRightException("All access right missing.");
		}
	}
	
	protected void checkJobsAllowed() {
		if (!getLoggedInUserRole().subsumes(UserRole.RUN_JOBS)) {
			throw new MissingRightException("Job access right missing.");
		}
	}
	
	protected void checkReadAllowed() {
		if (!getLoggedInUserRole().subsumes(UserRole.VIEW)) {
			throw new MissingRightException("Read access right missing.");
		}		
	}
	
	protected void checkIsMyJob(Job job) {
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			if (getLoggedInUser() == null || job.getUserId() != getLoggedInUser().getId()) {
				throw new MissingRightException("Job access right for job missing in role.");
			}
		}
	}

	protected void checkIsMyResource(NetFileResource res) {
		if (!getLoggedInUserRole().subsumes(UserRole.ADMIN)) {
			if (getLoggedInUser() == null || res.getUserId() != getLoggedInUser().getId()) {
				throw new MissingRightException("Resource access right for resource missing in role.");
			}
		}
	}
}
