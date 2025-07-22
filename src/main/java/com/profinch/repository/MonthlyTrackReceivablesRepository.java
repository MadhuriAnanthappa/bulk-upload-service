package com.profinch.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.MonthlyTrackReceivables;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class MonthlyTrackReceivablesRepository extends BaseRepository<MonthlyTrackReceivables> {
	@Value("${app.userid}")
	private String userId;

	private static final String INSERT_SQL = "INSERT INTO CLTB_MONTHLY_TRACK_UPLD "
			+ "(req_ref_no, loan_key, settlement_account,amt_to_deduct, narration, p_userid,notify_customer) "
			+ "VALUES (?, ?, ?, ?, ?, ?,?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_mand_val(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM clvw_montrckrecv_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM clvw_montrckrecv_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveBlocks(List<MonthlyTrackReceivables> monthlyTrackReceivables, String reqRefNo) {

		try {
			batchInsert(INSERT_SQL, monthlyTrackReceivables, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					MonthlyTrackReceivables ab = monthlyTrackReceivables.get(i);

					ps.setString(1, reqRefNo);
					ps.setString(2, ab.getLoanKey());
					ps.setString(3, ab.getSettlementAccount());
					ps.setString(4, ab.getAmtToDeduct());
					ps.setString(5, ab.getNarration());
					ps.setString(6, userId);
					ps.setString(7, ab.getNotifyCustomer());

				}

				@Override
				public int getBatchSize() {
					return monthlyTrackReceivables.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed: {}", e.getMessage(), e);

			return UploadStatus.FAILED;
		}
	}

	public void executeMonthlyTrackReceivables(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "monthlyTrackReceivablesDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM clvw_montrckrecv_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}
}
