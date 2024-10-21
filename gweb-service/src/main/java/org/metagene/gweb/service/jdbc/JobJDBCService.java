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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.gweb.service.JobService;
import org.metagene.gweb.service.NotFoundException;
import org.metagene.gweb.service.ServiceException;
import org.metagene.gweb.service.dto.DTO;
import org.metagene.gweb.service.dto.Job;
import org.metagene.gweb.service.dto.Job.JobStatus;
import org.metagene.gweb.service.dto.Job.JobType;
import org.metagene.gweb.service.dto.JobProgress;

public class JobJDBCService extends AbstractDTOJDBCService<Job> implements JobService {
	public JobJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public JobStatus enqueue(long jobId) {
		boolean commit = false;
		try {
			startTX();
			Job job = get(jobId);
			if (job != null) {
				if (compareStatusForJob(job, JobStatus.CREATED)) {
					job.setStatus(JobStatus.ENQUEUED);
					job.setEnqueued(new Date());
					updateInternal(job);
				}
				commit = true;
				return job.getStatus();
			} else {
				throw new NotFoundException();
			}
		} finally {
			endTX(commit);
		}
	}

	@Override
	public long enqueueDBInfo(long dbId, long userId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}

	@Override
	public long enqueueDBInstall(long dbId, long userId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}

	@Override
	public JobStatus cancel(long jobId) {
		boolean commit = false;
		try {
			startTX();
			Job job = get(jobId);
			if (job != null) {
				if (compareStatusForJob(job, JobStatus.ENQUEUED, JobStatus.STARTED)) {
					job.setStatus(
							JobStatus.ENQUEUED.equals(job.getStatus()) ? JobStatus.E_CANCELED : JobStatus.S_CANCELED);
					job.setFinished(new Date());
					updateInternal(job);
				}
				commit = true;
				return job.getStatus();
			} else {
				throw new NotFoundException();
			}
		} finally {
			endTX(commit);
		}
	}

	@Override
	public Job[] getByUser(long userId) {
		return getBySelection("select * from job where user_id = ? order by id", new PSFiller() {
			@Override
			public void fill(PreparedStatement ps) throws SQLException {
				ps.setLong(1, userId);
			}
		}).toArray(new Job[0]);
	}

	@Override
	public JobProgress getProgress(long jobId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Job fromResultSet(ResultSet rs) throws SQLException {
		Job res = new Job();
		res.setId(rs.getLong(1));
		res.setName(rs.getString(2));
		res.setFastqFile(rs.getString(3));
		res.setFastqFile2(rs.getString(4));
		long l = rs.getLong(5);
		res.setResourceId(rs.wasNull() ? DTO.INVALID_ID : l);
		l = rs.getLong(6);
		res.setResourceId2(rs.wasNull() ? DTO.INVALID_ID : l);
		res.setDbId(rs.getLong(7));
		res.setUserId(rs.getLong(8));
		res.setStatus(JobStatus.indexToValue(rs.getInt(9)));
		res.setEnqueued(convertTimestamp(rs.getTimestamp(10)));
		res.setStarted(convertTimestamp(rs.getTimestamp(11)));
		res.setFinished(convertTimestamp(rs.getTimestamp(12)));
		res.setCoveredBytes(rs.getLong(13));
		res.setJobType(JobType.indexToValue(rs.getInt(14)));
		res.setClassifyReads(rs.getBoolean(15));
		res.setErrorRate(rs.getDouble(16));

		return res;
	}

	@Override
	protected int toPreparedStatement(PreparedStatement ps, Job d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setString(2, d.getFastqFile());
		ps.setString(3, d.getFastqFile2());
		if (d.getResourceId() == -1) {
			ps.setNull(4, Types.BIGINT);
		} else {
			ps.setLong(4, d.getResourceId());
		}
		if (d.getResourceId2() == -1) {
			ps.setNull(5, Types.BIGINT);
		} else {
			ps.setLong(5, d.getResourceId2());
		}
		ps.setLong(6, d.getDbId());
		ps.setLong(7, d.getUserId());
		ps.setInt(8, d.getStatus().ordinal());
		ps.setTimestamp(9, convertTimestamp(d.getEnqueued()));
		ps.setTimestamp(10, convertTimestamp(d.getStarted()));
		ps.setTimestamp(11, convertTimestamp(d.getFinished()));
		ps.setLong(12, d.getCoveredBytes());
		ps.setInt(13, d.getJobType().ordinal());
		ps.setBoolean(14, d.isClassifyReads());
		ps.setDouble(15, d.getErrorRate());

		return 15;
	}

	@Override
	public long create(Job d) {
		d.setStatus(JobStatus.CREATED);
		d.setEnqueued(null);
		d.setStarted(null);
		d.setFinished(null);
		d.setCoveredBytes(0);
		return create(d,
				"insert into job(name, fastq_file, fastq_file_2, resource_id, resource_id2, db_id, user_id, status, enqueued, started, finished, covered_bytes, type, classify_reads, error_rate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
	}

	@Override
	public void remove(long id) {
		remove(id, "delete from job where id = ?");
	}

	@Override
	public Job get(long id) {
		return get(id, "select * from job where id = ?");
	}

	@Override
	public Job[] getAll() {
		return getAll("select * from job order by id").toArray(new Job[0]);
	}

	@Override
	public void update(Job d) {
		boolean commit = false;
		try {
			startTX();

			Job dOld = get(d.getId());
			// TODO: How about dates to be set for state changes?
			switch (dOld.getStatus()) {
			case CREATED:
				assertStatusForJob(d, JobStatus.CREATED, JobStatus.ENQUEUED);
				break;
			case ENQUEUED:
				assertStatusForJob(d, JobStatus.STARTED, JobStatus.E_CANCELED);
				break;
			case STARTED:
				assertStatusForJob(d, JobStatus.FINISHED, JobStatus.S_CANCELED);
				break;
			default:
				assertStatusForJob(dOld);
			}
			updateInternal(d);
			commit = true;
		} finally {
			endTX(commit);
		}
	}

	protected void updateInternal(Job d) {
		update(d,
				"update job set name = ?, fastq_file = ?, fastq_file_2 = ?, resource_id = ?, resource_id2 = ?, db_id = ?, user_id = ?, status = ?, enqueued = ?, started = ?, finished = ?, covered_bytes = ?, type = ?, classify_reads = ?, error_rate = ? where id = ?");
	}

	@Override
	public JobStatus[] getStatusForJobs(long[] jobIds) {
		List<Job> jobs = getAll("select * from job order by id");
		JobStatus[] res = new JobStatus[jobIds.length];

		Job j = new Job();

		for (int i = 0; i < jobIds.length; i++) {
			j.setId(jobIds[i]);
			int index = Collections.binarySearch(jobs, j, ID_DTO_COMPARATOR);
			if (index >= 0) {
				res[i] = jobs.get(index).getStatus();
			}
		}

		return res;
	}

	@Override
	public long[] getActiveJobIds() {
		return getJobIdsByStatus(null);
	}

	@Override
	public long[] getJobIdsByStatus(JobStatus status) {
		Connection c = null;
		try {
			c = getConnection();
			String statusClause = status == null
					? "status = " + JobStatus.ENQUEUED.ordinal() + " or status = " + JobStatus.STARTED.ordinal()
					: "status = ?";

			PreparedStatement ps = c.prepareStatement("select id, type <> " + JobType.UPLOAD_MATCH.ordinal()
					+ " as now from job where " + statusClause + " " + getOrderByStatus(status));
			if (status != null) {
				ps.setInt(1, status.ordinal());
			}
			ResultSet rs = ps.executeQuery();
			List<Long> l = new ArrayList<Long>();
			while (rs.next()) {
				l.add(rs.getLong(1));
			}
			return toArray(l);
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	protected String getOrderByStatus(JobStatus status) {
		if (status == null) {
			return "order by now, enqueued, started";
		}
		switch (status) {
		case E_CANCELED:
		case S_CANCELED:
		case FINISHED:
			return "order by now, jobfinished";
		case ENQUEUED:
			return "order by now, enqueued";
		case STARTED:
			return "order by now, started";
		default:
			return "";
		}
	}

	@Override
	public File getCSVFile(long jobId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}

	@Override
	public File getLogFile(long jobId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}

	@Override
	public boolean isCSVExists(long jobId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}

	@Override
	public boolean isLogExists(long jobId) {
		throw new UnsupportedOperationException("Not possible on this service level.");
	}
}
