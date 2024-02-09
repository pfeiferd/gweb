package org.metagene.genestrip.service.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.metagene.genestrip.service.JobService;
import org.metagene.genestrip.service.ServiceException;
import org.metagene.genestrip.service.dto.Job;
import org.metagene.genestrip.service.dto.Job.JobStatus;
import org.metagene.genestrip.service.dto.JobProgress;
import org.metagene.genestrip.service.dto.JobResult;

public class JobJDBCService extends AbstractDTOJDBCService<Job> implements JobService {
	public JobJDBCService(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public synchronized JobStatus enqueue(long jobId) {
		Job job = get(jobId);
		if (job != null) {
			if (JobStatus.CREATED.equals(job.getStatus())) {
				job.setStatus(JobStatus.ENQUEUED);
				job.setEnqueued(new Date());
				update(job);
			}
			return job.getStatus();
		}
		return null;
	}

	@Override
	public synchronized JobStatus cancel(long jobId) {
		Job job = get(jobId);
		if (job != null) {
			if (!JobStatus.FINISHED.equals(job.getStatus()) && !JobStatus.CANCELED.equals(job.getStatus())) {
				job.setStatus(JobStatus.CANCELED);
				job.setFinished(new Date());
				update(job);
			}
			return job.getStatus();
		}
		return null;
	}

	@Override
	public List<Job> getByUser(long userId) {
		return getBySelection("select * from job where user_id = ?", new PSFiller() {
			@Override
			public void fill(PreparedStatement ps) throws SQLException {
				ps.setLong(1, userId);
			}
		});
	}

	@Override
	public JobResult getResult(long jobId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public JobProgress getProgress(long jobId) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Job fromResultSet(ResultSet rs) throws SQLException {
		Job res = new Job(rs.getString(3), rs.getString(4), rs.getLong(5), rs.getLong(6));
		res.setName(rs.getString(2));
		res.setStatus(JobStatus.indexToValue(rs.getInt(7)));
		res.setId(rs.getLong(1));
		res.setEnqueued(convertTimestamp(rs.getTimestamp(8)));
		res.setStarted(convertTimestamp(rs.getTimestamp(9)));
		res.setFinished(convertTimestamp(rs.getTimestamp(10)));

		return res;
	}

	@Override
	protected int toPreparedStatement(PreparedStatement ps, Job d) throws SQLException {
		ps.setString(1, d.getName());
		ps.setString(2, d.getFastqFile());
		ps.setString(3, d.getFastqFile2());
		ps.setLong(4, d.getDbId());
		ps.setLong(5, d.getUserId());
		ps.setInt(6, d.getStatus().ordinal());
		ps.setTimestamp(7, convertTimestamp(d.getEnqueued()));
		ps.setTimestamp(8, convertTimestamp(d.getStarted()));
		ps.setTimestamp(9, convertTimestamp(d.getFinished()));

		return 9;
	}

	@Override
	public long create(Job d) {
		d.setStatus(JobStatus.CREATED);
		d.setEnqueued(null);
		d.setStarted(null);
		d.setFinished(null);
		return create(d, "insert into job(name, fastq_file, fastq_file_2, db_id, user_id, status, enqueued, started, finished) values (?,?,?,?,?,?,?,?,?)");
	}

	@Override
	public boolean remove(long id) {
		return remove(id, "delete from job where id = ?");
	}

	@Override
	public Job get(long id) {
		return get(id, "select * from job where id = ?");
	}

	@Override
	public List<Job> getAll() {
		return getAll("select * from job");
	}

	@Override
	public boolean update(Job d) {
		return update(d,
				"update job set name = ?, fastq_file = ?, fastq_file_2 = ?, db_id = ?, user_id = ?, status = ?, enqueued = ?, started = ?, finished = ? where id = ?");
	}

	@Override
	public List<Long> getPendingJobIds() {
		Connection c = null;
		try {
			c = getConnection();

			PreparedStatement ps = c.prepareStatement("select id from job where status = ? order by started");
			ps.setInt(1, JobStatus.ENQUEUED.ordinal());
			ResultSet rs = ps.executeQuery();
			List<Long> l = new ArrayList<Long>();
			while (rs.next()) {
				l.add(rs.getLong(1));
			}
			return l;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			releaseConnection(c);
		}
	}

	@Override
	public List<Long> getRunningJobIds() {
		throw new UnsupportedOperationException();
	}
}
