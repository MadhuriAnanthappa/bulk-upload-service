package com.profinch.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcUtil {
	
	 public static String executeProcedure(JdbcTemplate jdbcTemplate, String procedureCall, List<Object> args) {
	        return jdbcTemplate.execute(new CallableStatementCreator() {
	            @Override
	            public CallableStatement createCallableStatement(Connection con) throws SQLException {
	                CallableStatement cs = con.prepareCall(procedureCall);
	                for (int i = 0; i < args.size(); i++) {
	                    cs.setObject(i + 1, args.get(i));
	                }
	                return cs;
	            }
	        }, new CallableStatementCallback<String>() {
	            @Override
	            public String doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {
	                try {
	                    cs.execute();
	                    return "SUCCESS";
	                } catch (SQLException e) {
	                    log.error("Procedure execution failed: {}", e.getMessage(), e);
	                    return "FAILURE: " + e.getMessage();
	                }
	            }
	        });
	    }

}
