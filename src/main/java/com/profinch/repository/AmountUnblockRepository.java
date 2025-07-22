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
import com.profinch.entity.AmountUnblock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class AmountUnblockRepository extends BaseRepository<AmountUnblock> {
	@Value("${app.userid}")
	private String userId;

	private static final String INSERT_SQL = "INSERT INTO CATB_AMOUNT_UNBLOCK_UPLOAD "
			+ "(req_ref_no, ext_sys_user, insert_time,sl_no, account_no, release_amount, narration, status_flag, created_at,notify_customer) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_val_amountunblock_job(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM cavw_amountunblk_master_status WHERE req_ref_no = ?";
	
	private static final String DETAIL_QUERY = "SELECT * FROM cavw_amountunblk_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveBlocks(List<AmountUnblock> amountBlocks, String reqRefNo) {

		try {
			batchInsert(INSERT_SQL, amountBlocks, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					AmountUnblock ab = amountBlocks.get(i);
					Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

					ps.setString(1, reqRefNo);
					ps.setString(2, userId);
					ps.setTimestamp(3, currentTimestamp);
					ps.setString(4, "1");
					ps.setString(5, ab.getAccountno());
					ps.setDouble(6, ab.getReleaseAmount());
					ps.setString(7, ab.getNarration());
					ps.setString(8, "U");
					ps.setTimestamp(9, currentTimestamp);
					ps.setString(10, ab.getNotifyCustomer());
				}

				@Override
				public int getBatchSize() {
					return amountBlocks.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed: {}", e.getMessage(), e);

			return UploadStatus.FAILED;
		}
	}

	public void executeAmountUnblock(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "amountUnblockDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM CAVW_AMOUNTUNBLK_MASTER_STATUS WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}

}
