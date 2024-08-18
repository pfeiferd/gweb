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
package org.metagene.gweb.service.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.metagene.gweb.service.ResourceService;
import org.metagene.gweb.service.dto.NetFileResource;
import org.metagene.gweb.service.dto.NetFileResource.ResourceType;

public class ResourceJDBCService extends AbstractDTOJDBCService<NetFileResource> implements ResourceService {
	public ResourceJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	protected NetFileResource fromResultSet(ResultSet rs) throws SQLException {
		NetFileResource res = new NetFileResource();
		res.setId(rs.getLong(1));
		res.setName(rs.getString(2));
		res.setType(ResourceType.indexToValue(rs.getInt(3)));
		res.setUserId(rs.getLong(4));
		res.setUrl(rs.getString(5));

		return res;
	}

	@Override
	protected int toPreparedStatement(PreparedStatement ps, NetFileResource d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setInt(2, d.getType().ordinal());
		ps.setLong(3, d.getUserId());
		ps.setString(4, d.getUrl());

		return 4;
	}

	@Override
	public long create(NetFileResource d) {
		return create(d,
				"insert into net_file_resource(name, type, user_id, url) values (?,?,?,?)");
	}

	@Override
	public void remove(long id) {
		remove(id, "delete from net_file_resource where id = ?");
	}

	@Override
	public NetFileResource get(long id) {
		return get(id, "select * from net_file_resource where id = ?");
	}

	@Override
	public NetFileResource[] getAll() {
		return getAll("select * from net_file_resource order by id").toArray(new NetFileResource[0]);
	}

	@Override
	public void update(NetFileResource d) {
		update(d,
				"update net_file_resource set name = ?, type = ?, user_id = ?, url = ? where id = ?");
	}
	
	@Override
	public NetFileResource[] getByUser(long userId) {
		return getBySelection("select * from net_file_resource where user_id = ? order by id", new PSFiller() {
			@Override
			public void fill(PreparedStatement ps) throws SQLException {
				ps.setLong(1, userId);
			}
		}).toArray(new NetFileResource[0]);
	}
}
