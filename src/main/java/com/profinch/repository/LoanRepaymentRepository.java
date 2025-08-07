package com.profinch.repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.LoanRepayment;
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
public class LoanRepaymentRepository extends BaseRepository<LoanRepayment> {

    @Value("${app.userid}")
    private String userId;

    private static final String INSERT_SQL = "INSERT INTO CLTB_LOAN_REPAYMENT_UPLD "
            + "(customer_name, employee_identification_number, req_ref_no, deduction, file_name, insert_time, "
            + "status, uploader, upload_date, recon_number, recon_brn, deduction_code,notify_customer) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";


    private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_val_loanrepayment_job(?, ?)}";

    private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records "
            + "FROM cavw_loanrepayment_master_status WHERE req_ref_no = ?";
    
    private static final String DETAIL_QUERY = "SELECT * FROM cavw_loanrepayment_detail_status WHERE req_ref_no = ?";

    public UploadStatus saveRepayments(List<LoanRepayment> loanRepayments, String reqRefNo, String fileName) {
        try {
            batchInsert(INSERT_SQL, loanRepayments, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    LoanRepayment lr = loanRepayments.get(i);
                    Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());

                    ps.setString(1, lr.getCustomerName());                          
                    ps.setString(2, lr.getEmployeeIdentificationNumber());         
                    ps.setString(3, reqRefNo);                                    
                    ps.setDouble(4, lr.getDeduction());                            
                    ps.setString(5, fileName);                                    
                    ps.setTimestamp(6, currentTimestamp);                         
                    ps.setString(7, "U");                                         
					ps.setString(8, userId);                                      
                    ps.setTimestamp(9, currentTimestamp);                        
                    ps.setString(10, lr.getReconNumber());                        
                    ps.setString(11, lr.getReconBrn());                            
                    ps.setString(12, lr.getDeductionCode());     
                    ps.setString(13, lr.getNotifyCustomer());
                }

                @Override
                public int getBatchSize() {
                    return loanRepayments.size();
                }
            });

            return UploadStatus.SUCCESS;

        } catch (Exception e) {
            log.error("Batch insert failed for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
            return UploadStatus.FAILED;
        }
    }

    public void executeLoanRepayment(String reqRefNo, String userId) {
        executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId), false);
    }

    public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
        return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "loanRepaymentDetails");
    }

    public boolean existsByReqRefNo(String reqRefNo) {
        String sql = "SELECT 1 FROM cavw_loanrepayment_master_status WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
        try {
            jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
            return true;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return false;
        }
    }
}