package org.metagene.genestrip.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import org.metagene.genestrip.service.create.ServiceCreator;
import org.metagene.genestrip.service.create.ServiceCreator.Config;
import org.metagene.genestrip.service.dto.DTO;

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
		dataSource.setUrl("jdbc:hsqldb:file:./gwebtest");
		dataSource.setMaxActive(1);
		dataSource.setMaxIdle(1);
		dataSource.setInitialSize(1);

		// Clear the DB before test.
		Connection c = dataSource.getConnection();
		c.createStatement().execute("DROP SCHEMA PUBLIC CASCADE");
		c.close();

		serviceCreator = new ServiceCreator(dataSource, new Config() {
			@Override
			public String getConfigValue(String param) {
				if (ServiceCreator.INIT_ADMIN.equals(param)) {
					return Boolean.toString(isInitAdmin());
				}
				return null;
			}
		});
	}

	protected boolean isInitAdmin() {
		return false;
	}

	@Test
	public void testCRUDMethods() {
		CRUDService<D> service = serviceCreator.getBasicService(getServiceClass());

		D newD = newDTO();
		long id = service.create(newD);
		newD.setId(id);
		D d = service.get(id);

		assertEquals(id, d.getId());
		assertTrue(dtosEqual(newD, d));

		assertEquals(1, service.getAll().size());
		D d2 = service.getAll().get(0);
		assertEquals(id, d2.getId());
		assertTrue(dtosEqual(d, d2));

		if (isWithUpdate()) {
			updateDTO(d);
			service.update(d);
			D d3 = service.get(id);
			assertEquals(id, d3.getId());
			assertTrue(dtosEqual(d, d3));
		}

		assertEquals(1, service.getAll().size());
		D d4 = service.getAll().get(0);
		assertEquals(id, d4.getId());
		assertTrue(dtosEqual(d, d4));

		assertTrue(service.remove(id));
		assertNull(service.get(id));
		assertEquals(0, service.getAll().size());
		assertFalse(service.remove(id));
	}
	
	protected boolean isWithUpdate() {
		return true;
	}

	protected abstract Class<S> getServiceClass();

	protected abstract D newDTO();

	protected abstract void updateDTO(D d);

	protected abstract boolean dtosEqual(D d1, D d2);
}
