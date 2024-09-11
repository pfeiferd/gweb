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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.metagene.gweb.service.InstallService;
import org.metagene.gweb.service.ServiceException;
import org.metagene.gweb.service.dto.DB;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.NetFileResource;
import org.metagene.gweb.service.dto.Person;
import org.metagene.gweb.service.dto.User;

public class InstallJDBCService extends AbstractJDBCService implements InstallService {
	public enum SQLDialect {
		HSQL, POSTGRES
	}

	private static final String[][] HSQL_DDL = { {
			"create table if not exists schema_version (id bigint not null primary key, version integer not null)",
			"create table if not exists person (id bigint not null primary key generated by default as identity, first_name varchar("
					+ Person.FIRST_NAME_SIZE + ") not null, last_name varchar(" + Person.LAST_NAME_SIZE + ") not null)",
			"create table if not exists sysuser (id bigint not null primary key generated by default as identity, login varchar("
					+ User.LOGIN_SIZE
					+ ") unique not null, role integer not null, person_id bigint not null, constraint fk_person_id foreign key(person_id) references person(id) on update cascade on delete no action)",
			"create table if not exists db (id bigint not null primary key generated by default as identity, name varchar("
					+ DB.NAME_SIZE + ") unique not null, db_file_prefix varchar(" + DB.DB_FILE_PREFIX_SIZE
					+ ") unique not null)",
			"create table if not exists net_file_resource (id bigint not null primary key generated by default as identity, name varchar("
					+ NetFileResource.NAME_SIZE + ") not null, type int not null, user_id bigint not null, url varchar("
					+ NetFileResource.URL_SIZE
					+ "), constraint fk_res_user_id foreign key(user_id) references sysuser(id) on update cascade on delete no action)",
			"create table if not exists job (id bigint not null primary key generated by default as identity, name varchar("
					+ Job.NAME_SIZE + ") not null, fastq_file varchar(" + Job.FASTQ_FILE_SIZE
					+ "), fastq_file_2 varchar(" + Job.FASTQ_FILE_SIZE
					+ "), resource_id bigint, resource_id2 bigint, db_id bigint not null, user_id bigint not null, status int not null, enqueued timestamp, started timestamp, finished timestamp, covered_bytes bigint not null, type int not null, "
					+ " constraint fk_db_id foreign key(db_id) references db(id) on update cascade on delete no action, constraint fk_user_id foreign key(user_id) references sysuser(id) on update cascade on delete no action,"
					+ " constraint fk_resource_id foreign key(resource_id) references net_file_resource(id) on update cascade on delete no action,"
					+ " constraint fk_resource_id2 foreign key(resource_id2) references net_file_resource(id) on update cascade on delete no action)" },
			{ "alter table sysuser add column password binary(32) not null" },
			{ "alter table job add column classify_reads boolean default false not null" },
			{ "alter table db add column install_url varchar(" + DB.URL_SIZE + ")" },
			{ "alter table db add column install_md5 varchar(" + DB.MD5_SIZE + ")" },
			{ "alter table job add column error_rate double precision default 0.5 not null" } };

	private final SQLDialect dialect;

	public InstallJDBCService(DataSource dataSource, SQLDialect dialect) {
		super(dataSource);
		this.dialect = dialect == null ? SQLDialect.HSQL : dialect;
	}

	@Override
	public boolean install() {
		Connection c = null;
		try {
			c = getConnection();

			String[][] DDL = getDDLForDialect(dialect);
			int currentVersion = getCurrenVersion(c);
			for (int version = currentVersion + 1; version < DDL.length; version++) {
				try (Statement s = c.createStatement()) {
					for (int i = 0; i < DDL[version].length; i++) {
						s.execute((DDL[version])[i]);
					}
				}
				if (version == 0) {
					try (Statement s = c.createStatement()) {
						s.executeUpdate("insert into schema_version values (0,1);");
					}
				} else {
					try (PreparedStatement p = c
							.prepareStatement("update schema_version set version = ? where id = 0")) {
						p.setInt(1, version);
						p.executeUpdate();
					}
				}
			}
			return currentVersion == -1;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected int getCurrenVersion(Connection c) throws SQLException {
		int version = -1;
		DatabaseMetaData meta = c.getMetaData();

		// Beware: "SCHEMA_VERSION" must be upper case - otherwise HSQL won't find it (a
		// bug apparently in HSQL). BUT: It must be lower case for Postgres (a bug too?)
		String schemaTableName = "schema_version";
		if (SQLDialect.HSQL.equals(dialect)) {
			schemaTableName = schemaTableName.toUpperCase();
		}
		try (ResultSet resultSet = meta.getTables(null, null, schemaTableName, new String[] { "TABLE" })) {
			if (resultSet.next()) {
				try (Statement versionCheck = c.createStatement()) {
					try (ResultSet rs = versionCheck.executeQuery("select version from schema_version where id = 0")) {
						if (rs.next()) {
							version = rs.getInt(1);
						}
					}
				}
			}
		}

		return version;
	}

	protected String[][] getDDLForDialect(SQLDialect dialect) {
		switch (dialect) {
		case HSQL:
			return HSQL_DDL;
		case POSTGRES:
			HSQL_DDL[1][0] = "alter table sysuser add column password bytea not null";
			return HSQL_DDL;
		default:
			throw new IllegalStateException("Should never happen.");
		}
	}
}
