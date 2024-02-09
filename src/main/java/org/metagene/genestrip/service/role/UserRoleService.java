package org.metagene.genestrip.service.role;

import org.metagene.genestrip.service.UserService;
import org.metagene.genestrip.service.dto.User;

public class UserRoleService extends CRUDRoleService<UserService, User> implements UserService {
	public UserRoleService(UserService delegate, UserStore userStore) {
		super(delegate, userStore);
	}

	@Override
	public User login(String login, String password) {
		if (getLoggedInUser() != null) {
			logout();
		}
		User u = getDelegate().login(login, password);
		setLoggedInUser(u);
		return u;
	}

	@Override
	public void logout() {
		getDelegate().logout();
		setLoggedInUser(null);
	}
}
