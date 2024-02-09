package org.metagene.genestrip.service.role;

import org.metagene.genestrip.service.dto.User;

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
	
	protected void setLoggedInUser(User user) {
		userStore.setUser(user);
	}
	
	protected void checkAllAllowed() {
		if (getLoggedInUser() == null || !getLoggedInUser().isAllowAll()) {
			throw new MissingRightException("All access right missing.");
		}
	}
	
	protected void checkJobsAllowed() {
		if (getLoggedInUser() == null || (!getLoggedInUser().isAllowJobs() && !getLoggedInUser().isAllowAll())) {
			throw new MissingRightException("Job access right missing.");
		}
	}
	
	protected void checkReadAllowed() {
		if (getLoggedInUser() == null) {
			throw new MissingRightException("Read access right missing.");
		}		
	}
}
