package com.profinch.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.AccountReclassification;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AccountReclassificationRepository extends BaseRepository<AccountReclassification> {
	@Value("${app.userid}")
	private String userId;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String INSERT_SQL = "INSERT INTO STTB_ACCHANGE_UPLOAD_CUST ("
			+ "cust_ac_no, new_class, req_ref_no, sms_flag, status, online_charge_plan, offline_charge_plan, notify_customer"
			+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_reclassify_val(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM stvw_acchange_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM stvw_acchange_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveAccountReclassifications(List<AccountReclassification> accountReclassifications,
			String reqRefNo) {

		try {
			batchInsert(INSERT_SQL, accountReclassifications, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					AccountReclassification accReclass = accountReclassifications.get(i);

					ps.setString(1, accReclass.getAccountNo());
					ps.setString(2, accReclass.getNewAcCls());
					ps.setString(3, reqRefNo);
					ps.setString(4, accReclass.getSmsRequired());
					ps.setString(5, "U");
					ps.setString(6, accReclass.getOnlineChargePlanString());
					ps.setString(7, accReclass.getOfflineChargePlanString());
					ps.setString(8, accReclass.getNotifyCustomer());

				}

				@Override
				public int getBatchSize() {
					return accountReclassifications.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed: {}", e.getMessage(), e);

			return UploadStatus.FAILED;
		}
	}

	public void executeValidationJob(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId),true);
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "accountReclassificationDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM stvw_acchange_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}

}
