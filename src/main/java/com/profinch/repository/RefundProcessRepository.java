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
import com.profinch.entity.RefundProcess;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Repository
public class RefundProcessRepository extends BaseRepository<RefundProcess> {
	
	@Value("${app.userid}")
    private String userId;

	private static String INSERT_SQL = "insert into cltb_pmnt_refund_upld_cust"
			+ "(empno , name , debit_amount , status ,err_code , ac_desc , cust_no ,account_to_refund ,  "
			+ "account_number , employer	, recon_no , recon_brn , excess_gl ,narration,req_ref_no,p_userid,notify_customer,txn_brn)"
			+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_refund_val(?, ?)}";
	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM clvw_refund_process_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM clvw_refund_process_detail_status WHERE req_ref_no = ?";
	
	public UploadStatus saveRecord(List<RefundProcess> processes, String reqRefNo) {
		try {
			batchInsert(INSERT_SQL, processes, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					RefundProcess process = processes.get(i);
					ps.setString(1, process.getEmpNo());
					ps.setString(2, process.getName());
					ps.setDouble(3, process.getDebitAmount());
					ps.setString(4, process.getStatus());
					ps.setString(5, process.getErrorCode());
					ps.setString(6, process.getAcDesc());
					ps.setString(7, process.getCustNo());
					ps.setString(8, process.getRefundAccount());
					ps.setString(9, process.getAccountNo());
					ps.setString(10, process.getEmployer());
					ps.setString(11, process.getReconNo());
					ps.setString(12, process.getReconBrn());
					ps.setString(13, process.getExcessGL());
					ps.setString(14, process.getNarration());
					ps.setString(15, reqRefNo);
					ps.setString(16, userId);
					ps.setString(17, process.getNotifyCustomer());
					ps.setString(18, process.getTxtBrn());
				}

				@Override
				public int getBatchSize() {
					// TODO Auto-generated method stub
					return processes.size();
				}
			});
			return UploadStatus.SUCCESS;
		} catch (Exception e) {
			log.error("Batch insert failed: {}", e.getMessage(), e);
			return UploadStatus.FAILED;
		}
	}

	public void executeRefundProcess(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId));
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "refundProcessDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM clvw_refund_process_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}
	
}