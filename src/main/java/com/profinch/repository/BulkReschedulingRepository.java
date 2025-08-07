package com.profinch.repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.BulkRescheduling;
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
public class BulkReschedulingRepository extends BaseRepository<BulkRescheduling> {


    private static final String INSERT_SQL = "INSERT INTO CLTB_ACCT_RESCH_UPLD_CUST "
            + "(req_ref_no, loan_account, file_name, insert_time, status, uploader, notify_customer) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_val_bulkrescheduling_job(?, ?)}";

    private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records "
            + "FROM cavw_bulkrescheduling_master_status WHERE req_ref_no = ?";
    
    private static final String DETAIL_QUERY = "SELECT * FROM cavw_bulkrescheduling_detail_status WHERE req_ref_no = ?";

    public UploadStatus saveReschedules(List<BulkRescheduling> reschedules, String reqRefNo, String fileName, String userId) {
        try {
            batchInsert(INSERT_SQL, reschedules, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BulkRescheduling br = reschedules.get(i);
                    Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, reqRefNo);
                    ps.setString(2, br.getLoanAccount());
                    ps.setString(3, fileName);
                    ps.setTimestamp(4, currentTimestamp);
                    ps.setString(5, "U");  // Status Flag: U = Unprocessed
                    ps.setString(6, userId);
                    ps.setString(7, br.getNotifyCustomer());
                }

                @Override
                public int getBatchSize() {
                    return reschedules.size();
                }
            });

            return UploadStatus.SUCCESS;

        } catch (Exception e) {
            log.error("Batch insert failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
            return UploadStatus.FAILED;
        }
    }

    public void executeBulkRescheduling(String reqRefNo, String userId) {
        executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId), false);
    }

    public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
        return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "bulkReschedulingDetails");
    }

    public boolean existsByReqRefNo(String reqRefNo) {
        String sql = "SELECT 1 FROM cavw_bulkrescheduling_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
        try {
            jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
            return true;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }
}