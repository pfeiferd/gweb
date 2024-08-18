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
package org.metagene.gweb.service.dto;

public class User extends DTO {
	public enum UserRole {
		NONE, NO_LOGIN, VIEW, RUN_JOBS, ADMIN;

		private static final UserRole[] ROLE_VALUES = UserRole.values();
		
		public static UserRole indexToValue(int index) {
			if (index >= 0 && index < ROLE_VALUES.length) {
				return ROLE_VALUES[index];
			}
			return null;
		}
		
		public boolean subsumes(UserRole role) {
			return (ordinal() - role.ordinal()) >= 0;
		}
	}
	
	public static int PASSWORD_SIZE = 50;
	public static int LOGIN_SIZE = 50;
	
	private static final long serialVersionUID = 1L;

	private String login;
	private String password; // TODO: Password should be hashed.
	private long personId;

	private UserRole role;
	
	public User() {
	}
	
	public UserRole getRole() {
		return role;
	}
	
	public void setRole(UserRole role) {
		this.role = role;
	}

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}
	
	public boolean checkPersonIdValid() {
		return isValidId(personId);
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	
	public boolean checkLoginValid() {
		return login != null && !login.isEmpty();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean checkPasswordValid() {
		// Updated users may have a null password - this means the password will not be updated.
		return  checkIdValid() ?  (password == null || !password.isEmpty()) : (password != null && !password.isEmpty());
	}
	
	public boolean checkRoleValid() {
		return role != null;
	}

	@Override
	public boolean checkValid() {
		return super.checkValid() && checkPersonIdValid() && checkLoginValid() && checkPasswordValid() && checkRoleValid();
	}
}
