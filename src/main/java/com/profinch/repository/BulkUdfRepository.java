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
import com.profinch.entity.BulkUDF;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BulkUdfRepository extends BaseRepository<BulkUDF> {
	@Value("${app.userid}")
	private String userId;
	private static final String INSERT_SQL = "INSERT INTO catb_bulk_udf_upld_cust"
			+ "(req_ref_no , cust_acc_no , udf_field , udf_value , status , auth_stat , process_date , p_userid,notify_customer) "
			+ "values (?, ?, ?, ?, ?, ?,CURRENT_TIMESTAMP, ?,?)";

	private static final String VALIDATION_PROC = "{call stpks_bulk_api_upload.pr_acc_val(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM stvw_bulkudf_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM stvw_bulkudfdetail_status WHERE req_ref_no = ?";

	public UploadStatus saveRecord(List<BulkUDF> bulkUdf, String reqRefNo) {
		try {
			batchInsert(INSERT_SQL, bulkUdf, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					BulkUDF udf = bulkUdf.get(i);
					ps.setString(1, reqRefNo);
					ps.setString(2, udf.getAccNo());
					ps.setString(3, udf.getUdfField());
					ps.setString(4, udf.getUdfVal());
					ps.setString(5, "P");
					ps.setString(6, "A");
					ps.setString(7, userId);
					ps.setString(8, udf.getNotifyCustomer());
				}

				@Override
				public int getBatchSize() {
					return bulkUdf.size();
				}
			});

			return UploadStatus.SUCCESS;

		} catch (Exception e) {
			log.error("Batch insert failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
			return UploadStatus.FAILED;
		}

	}
	public void executeBulkUdfProc(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId),true);
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "bulkUdfDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM stvw_bulkudf_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}
}
