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
package org.metagene.gweb.service;

import java.sql.SQLException;

import org.junit.Before;
import org.metagene.gweb.service.dto.NetFileResource;
import org.metagene.gweb.service.dto.NetFileResource.ResourceType;

public class ResourceCRUDServiceTest extends AbstractCRUDServiceTest<ResourceService, NetFileResource> {
	private long userId;

	@Before
	@Override
	public void setUp() throws SQLException {
		super.setUp();

		userId = serviceCreator.getBasicService(UserService.class).getAll()[0].getId();
	}

	protected boolean isInitAdmin() {
		return true;
	}

	protected boolean isWithUpdate() {
		return false;
	}

	@Override
	protected boolean dtosEqual(NetFileResource d1, NetFileResource d2) {
		return d1.getName().equals(d2.getName()) && d1.getType() == d2.getType() && d1.getUserId() == d2.getUserId()
				&& d1.getUrl().equals(d2.getUrl());
	}

	@Override
	protected NetFileResource newDTO() {
		NetFileResource resource = new NetFileResource();
		resource.setName(nextString());
		resource.setType(ResourceType.HTTP_URL);
		resource.setUserId(userId);
		resource.setUrl("http://www.tagesschau.de");
		return resource;
	}

	@Override
	protected Class<ResourceService> getServiceClass() {
		return ResourceService.class;
	}

	@Override
	protected void updateDTO(NetFileResource d) {
		d.setName(nextString());
	}
}
