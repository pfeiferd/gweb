package org.metagene.genestrip.service.dto;

public class User extends DTO {
	private static final long serialVersionUID = 1L;
	
	private String login;
	private String password; // TODO: Password should be hashed.
	private long personId; 
	
	private boolean allowAll;
	private boolean allowJobs;
	
	public User() {
	}
	
	public long getPersonId() {
		return personId;
	}
	
	public void setPersonId(long personId) {
		this.personId = personId;
	}
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAllowAll() {
		return allowAll;
	}

	public void setAllowAll(boolean allowAll) {
		this.allowAll = allowAll;
	}

	public boolean isAllowJobs() {
		return allowJobs;
	}

	public void setAllowJobs(boolean allowJobs) {
		this.allowJobs = allowJobs;
	}
}
