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
import com.profinch.entity.CustomerCatMod;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class CustomerCatModRepository extends BaseRepository<CustomerCatMod> {
	@Value("${app.userid}")
	private String userId;
	private static final String INSERT_SQL = "INSERT INTO CATB_CUST_CAT_MOD_UPLD "
			+ "(cust_ac_no, new_category, status, auth_stat, req_ref_no, process_date, maker_id,notify_customer) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?,?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_custcat_val(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM stvw_custcatmod_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM stvw_custcatmod_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveCustomerCatMod(List<CustomerCatMod> blocks, String reqRefNo) {

		try {
			batchInsert(INSERT_SQL, blocks, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					CustomerCatMod ab = blocks.get(i);
					Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

					ps.setString(1, ab.getAccountno());
					ps.setString(2, ab.getNewCategory());
					ps.setString(3, "U");
					ps.setString(4, "U");
					ps.setString(5, reqRefNo);
					ps.setTimestamp(6, currentTimestamp);
					ps.setString(7, userId);
					ps.setString(8, ab.getNotifyCustomer());
				}

				@Override
				public int getBatchSize() {
					return blocks.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed: {}", e.getMessage(), e);

			return UploadStatus.FAILED;
		}
	}

	public void executeProcedure(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "customerCategoryDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM stvw_custcatmod_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}
}
