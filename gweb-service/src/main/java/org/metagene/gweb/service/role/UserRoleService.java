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
import org.metagene.gweb.service.UserService;
import org.metagene.gweb.service.dto.User;

public class UserRoleService extends CRUDRoleService<UserService, User> implements UserService, DefaultLoginService {
	public final String defaultUser;
	
	public UserRoleService(UserService delegate, UserStore userStore, String defaultUser) {
		super(delegate, userStore, null);
		this.defaultUser = defaultUser;
	}

	@Override
	public User defaultLogin() {
		return defaultUser != null ? login(defaultUser, defaultUser) : null;
	}

	@Override
	public User login(String login, String password) {
		if (super.getUserFromStore() != null) {
			logout();
		}
		User u = getDelegate().login(login, password);
		setUserInStore(u);
		return u;
	}

	@Override
	public void logout() {
		getDelegate().logout();
		setUserInStore(null);
	}
	
	@Override
	public User getLoggedInUser() {
		return super.getLoggedInUser();
	}
	
	@Override
	public User getByLogin(String login) {
		checkAllAllowed();
		return getDelegate().getByLogin(login);
	}
}
