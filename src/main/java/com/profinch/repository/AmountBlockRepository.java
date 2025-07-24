package com.profinch.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.profinch.common.UploadStatus;
import com.profinch.entity.AmountBlock;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class AmountBlockRepository extends BaseRepository<AmountBlock> {
	@Value("${app.userid}")
	private String userId;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final String INSERT_SQL = "INSERT INTO catb_amount_block_upload "
			+ "(req_ref_no, ext_sys_user, insert_time, account_no, block_amount, narration, expiry_date, status_flag, created_at,notify_customer) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String VALIDATION_PROC = "{call cspks_bulk_api_upload.pr_val_amountblock_job(?, ?)}";

	private static final String SUMMARY_QUERY = "SELECT total_records, processed_records, error_records FROM cavw_amountblock_master_status WHERE req_ref_no = ?";
	private static final String DETAIL_QUERY = "SELECT * FROM cavw_amountblock_detail_status WHERE req_ref_no = ?";

	public UploadStatus saveBlocks(List<AmountBlock> amountBlocks, String reqRefNo) {

		try {
			batchInsert(INSERT_SQL, amountBlocks, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					AmountBlock ab = amountBlocks.get(i);
					Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
					String defaultExpiry = "31-Dec-2099";
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

					Timestamp expiryTimestamp = null;
					try {
						expiryTimestamp = ab.getExpirydate() != null ? parseTimestamp(ab.getExpirydate())
								: new Timestamp(sdf.parse(defaultExpiry).getTime());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					ps.setString(1, reqRefNo);
					ps.setString(2, userId);
					ps.setTimestamp(3, currentTimestamp);
					ps.setString(4, ab.getAccountno());
					ps.setDouble(5, ab.getAmount());
					ps.setString(6, ab.getNarration());
					ps.setTimestamp(7, expiryTimestamp);
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

	public void executeValidationJob(String reqRefNo, String userId) {
		executeProcedure(VALIDATION_PROC, Arrays.asList(reqRefNo, userId),false);
	}

	public Map<String, Object> getStatusByReqRefNo(String reqRefNo) {
		return fetchStatus(SUMMARY_QUERY, DETAIL_QUERY, reqRefNo, "amountBlockDetails");
	}

	public boolean existsByReqRefNo(String reqRefNo) {
		String sql = "SELECT 1 FROM CAVW_AMOUNTBLOCK_MASTER_STATUS WHERE req_ref_no = ? FETCH FIRST 1 ROWS ONLY";
		try {
			jdbcTemplate.queryForObject(sql, Integer.class, reqRefNo);
			return true;
		} catch (org.springframework.dao.EmptyResultDataAccessException e) {
			return false;
		}
	}

	private Timestamp parseTimestamp(String input) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		try {
			return Timestamp.valueOf(LocalDateTime.parse(input, formatter));
		} catch (DateTimeParseException e) {
			try {
				return Timestamp.valueOf(LocalDate.parse(input, formatter).atStartOfDay());
			} catch (DateTimeParseException ex) {
				log.error("Invalid date format for input '{}'", input);
				throw ex;
			}
		}
	}

}
