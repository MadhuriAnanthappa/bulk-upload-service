package com.profinch.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.profinch.entity.User;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = new UserRowMapper();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM smtb_bulkupload_users_custom WHERE username = ? and status = 'O'";
        return jdbcTemplate.query(sql, userRowMapper, username).stream().findFirst();
    }

    public void save(User user) {
        String sql = "INSERT INTO smtb_bulkupload_users_custom (username, user_password, user_role,created_at,status) VALUES (?, ?, ?,?,?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getRole(),LocalDate.now(),"O");
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("user_password"));
            user.setRole(rs.getString("user_role"));
            return user;
        }
    }
}