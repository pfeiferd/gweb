package org.metagene.genestrip.service;

import org.metagene.genestrip.service.dto.User;

public interface UserService extends CRUDService<User> {
	public User login(String login, String password);
	public void logout();
}
