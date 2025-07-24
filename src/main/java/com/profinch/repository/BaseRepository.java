package com.profinch.repository;

import com.profinch.util.ColumnFormatUtil;
import com.profinch.util.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseRepository<T> {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Performs batch insert using the provided SQL and setter for binding parameters.
     */
    protected String batchInsert(String insertSql, List<T> batch, BatchPreparedStatementSetter setter) {
        try {
            jdbcTemplate.batchUpdate(insertSql, setter);
            return "SUCCESS";
        } catch (Exception e) {
            log.error("Batch insert failed: {}", e.getMessage(), e);
            return "FAILURE";
        }
    }

    /**
     * Executes a stored procedure with the given args.
     */
    protected void executeProcedure(String procedureCall, List<Object> args, boolean setNlsFormat) {
        JdbcUtil.executeProcedure(jdbcTemplate, procedureCall, args, setNlsFormat);
    }

    /**
     * Fetches status summary and detail by reqRefNo.
     * @param summarySql SQL to fetch summary counts
     * @param detailSql SQL to fetch detail records
     * @param reqRefNo Request reference number
     * @param detailKey The key name for detail records in returned map
     */
    protected Map<String, Object> fetchStatus(
            String summarySql,
            String detailSql,
            String reqRefNo,
            String detailKey) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Map<String, Object> summary = jdbcTemplate.queryForMap(summarySql, reqRefNo);
            List<Map<String, Object>> details = jdbcTemplate.queryForList(detailSql, reqRefNo);
            List<Map<String, Object>> camelCaseDetails = ColumnFormatUtil.convertListKeysToCamelCase(details);

            response.put("reqRefNo", reqRefNo);
            response.put("totalRecords", summary.getOrDefault("total_records", 0));
            response.put("processedRecords", summary.getOrDefault("processed_records", 0));
            response.put("errorRecords", summary.getOrDefault("error_records", 0));
            response.put(detailKey, camelCaseDetails);

        } catch (EmptyResultDataAccessException e) {
            log.warn("No status found for reqRefNo {}.", reqRefNo);
            response.put("reqRefNo", reqRefNo);
            response.put("totalRecords", 0);
            response.put("processedRecords", 0);
            response.put("errorRecords", 0);
            response.put(detailKey, Collections.emptyList());
        } catch (Exception e) {
            log.error("Error fetching status for reqRefNo {}: {}", reqRefNo, e.getMessage(), e);
            response.put("error", "Internal server error");
        }

        return response;
    }
}
