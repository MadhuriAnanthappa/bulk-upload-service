package com.profinch.repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.PermanentTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class PermanentTrackRepository extends BaseRepository<PermanentTrack> {
	@Value("${app.userid}")
	private String userId;

	private static final String INSERT_SQL = "INSERT INTO CLTB_PERMANENT_TRACK_UPLD "
			+ "(req_ref_no, ext_user, insert_time, batch_no, dr_branch, dr_account, cr_branch, cr_account, amount, instrument_code, narration,notify_customer, txn_code) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_ptrack_validate(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM clvw_permtrack_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM clvw_permtrack_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveBatch(List<PermanentTrack> permanentTracks, String reqRefNo) {
		try {
			batchInsert(INSERT_SQL, permanentTracks, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					PermanentTrack pt = permanentTracks.get(i);
					Timestamp now = Timestamp.valueOf(LocalDateTime.now());

					ps.setString(1, reqRefNo);
					ps.setString(2, userId);
					ps.setTimestamp(3, now);
					ps.setString(4, String.valueOf(pt.getBatchNo()));
					ps.setString(5, pt.getDrBranch());
					ps.setString(6, pt.getDrAccount());
					ps.setString(7, pt.getCrBranch());
					ps.setString(8, pt.getCrAccount());
					ps.setDouble(9, pt.getAmount());
					ps.setString(10, pt.getInstrumentCode());
					ps.setString(11, pt.getNarration());
					ps.setString(12, pt.getNotifyCustomer());
					ps.setString(13, pt.getTxnCode());
				}

				@Override
				public int getBatchSize() {
					return permanentTracks.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
			return UploadStatus.FAILED;
		}

	}

	public void executePermanentTrack(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "permanentTrackDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM CLVW_PERMTRACK_MASTER_STATUS WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}
}
