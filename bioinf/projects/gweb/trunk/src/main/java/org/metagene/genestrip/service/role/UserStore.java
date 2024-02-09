package org.metagene.genestrip.service.role;

import org.metagene.genestrip.service.dto.User;

public interface UserStore {
	public User getUser();
	public void setUser(User u);
}
