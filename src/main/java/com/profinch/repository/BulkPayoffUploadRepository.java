package com.profinch.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.BulkPayoff;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BulkPayoffUploadRepository extends BaseRepository<BulkPayoff> {
	
	@Value("${app.userid}")
	private String userId;

	private static final String INSERT_SQL = "INSERT INTO cltb_bulk_payoff_upld "
			+ "(req_ref_no,uploader,insert_time, loan_no,settle_acc,recon_no,ccy, amt,waive, narration,notify_customer) "
			+ "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_upload_payoff(?, ?)}";
	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM clvw_bulkpayoff_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM clvw_bulkpayoff_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveBatch(List<BulkPayoff> bulkPayoff, String reqRefNo) {
		try {
			batchInsert(INSERT_SQL, bulkPayoff, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					BulkPayoff bp = bulkPayoff.get(i);
					Timestamp now = Timestamp.valueOf(LocalDateTime.now());

					ps.setString(1, reqRefNo);
					ps.setString(2, userId); // Ideally fetched from context
					ps.setTimestamp(3, now);
					ps.setString(4, bp.getLoanNo());
					ps.setString(5, bp.getSettleAcc());
					ps.setString(6, bp.getReconNo());
					ps.setString(7, bp.getCcy());
					ps.setDouble(8, bp.getAmout());
					ps.setString(9, bp.getWaive());
					ps.setString(10, bp.getNarration());
					ps.setString(11, bp.getNotifyCustomer());
				}

				@Override
				public int getBatchSize() {
					return bulkPayoff.size();
				}
			});
			log.info("Batch {} for {} inserted successfully.", reqRefNo);
			return UploadStatus.SUCCESS;
		} catch (Exception e) {
			log.error("Failed to insert batch {}: {}", e.getMessage(), e);
			return UploadStatus.FAILED;
		}
	}

	public void executeBulkPayoffProc(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "bulkPayoffDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM clvw_bulkpayoff_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}

}
