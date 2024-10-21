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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.metagene.gweb.service.create.ServiceCreator;
import org.metagene.gweb.service.create.ServiceCreator.Config;
import org.metagene.gweb.service.create.ServiceCreator.Logger;
import org.metagene.gweb.service.dto.DTO;
import org.metagene.gweb.service.dummy.DummyServiceCreator;
import org.metagene.gweb.service.jdbc.InstallJDBCService.SQLDialect;

public abstract class AbstractCRUDServiceTest<S extends CRUDService<D>, D extends DTO> {
	protected ServiceCreator serviceCreator;

	private int c = 0;

	protected String nextString() {
		return Integer.toString(c++);
	}

	@Before
	public void setUp() throws SQLException {
		BasicDataSource dataSource = new BasicDataSource();

		dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		dataSource.setUrl("jdbc:hsqldb:file:./gwebtest;hsqldb.lock_file=false");
		dataSource.setMaxActive(1);
		dataSource.setMaxIdle(1);
		dataSource.setInitialSize(1);

		// Clear the DB before test.
		Connection c = dataSource.getConnection();
		c.createStatement().execute("DROP SCHEMA PUBLIC CASCADE");
		c.close();

		serviceCreator = new DummyServiceCreator(dataSource, new Config() {
			@Override
			public String getConfigValue(String param) {
				if (ServiceCreator.INIT_ADMIN.equals(param)) {
					return Boolean.toString(isInitAdmin());
				}
				else if (ServiceCreator.SQL_DIALECT.equals(param)) {
					return SQLDialect.HSQL.name();
				}
				else {
					return null;
				}
			}
			
			@Override
			public Object getConfigAttribute(String param) {
				return null;
			}
		}, new Logger() {
			@Override
			public void log(String message) {
				System.err.println("Log: " + message);
			}
			
			@Override
			public void log(String message, Throwable t) {
				System.err.println("Log: " + message);
				t.printStackTrace();
			}
		});
	}

	protected boolean isInitAdmin() {
		return false;
	}

	@Test
	public void testCRUDMethods() {
		CRUDService<D> service = serviceCreator.getBasicService(getServiceClass());
		
		int initial = service.getAll().length;

		D newD = newDTO();
		long id = service.create(newD);
		newD.setId(id);
		D d = service.get(id);

		assertEquals(id, d.getId());
		assertTrue(dtosEqual(newD, d));

		assertEquals(initial + 1, service.getAll().length);
		// This relies on the ordering of getAll() results ("order by id")
		D d2 = service.getAll()[initial + 0];
		assertEquals(id, d2.getId());
		assertTrue(dtosEqual(d, d2));

		if (isWithUpdate()) {
			updateDTO(d);
			service.update(d);
			D d3 = service.get(id);
			assertEquals(id, d3.getId());
			assertTrue(dtosEqual(d, d3));
		}

		assertEquals(initial + 1, service.getAll().length);
		D d4 = service.getAll()[initial + 0];
		assertEquals(id, d4.getId());
		assertTrue(dtosEqual(d, d4));

		service.remove(id);
		assertNull(service.get(id));
		assertEquals(initial + 0, service.getAll().length);
		try {
			service.remove(id);
			fail();
		} catch (NotFoundException e) {			
		}
	}
	
	protected boolean isWithUpdate() {
		return true;
	}

	protected abstract Class<S> getServiceClass();

	protected abstract D newDTO();

	protected abstract void updateDTO(D d);

	protected abstract boolean dtosEqual(D d1, D d2);
}
