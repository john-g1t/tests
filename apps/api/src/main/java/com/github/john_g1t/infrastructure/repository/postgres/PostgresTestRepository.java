package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.Test;
import com.github.john_g1t.domain.repository.TestRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresTestRepository implements TestRepository {
    private final static String INSERT = "INSERT INTO tests (title, description, created_by, time_limit, max_attempts, is_active, start_time, end_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    private final static String UPDATE = "UPDATE tests SET title = ?, description = ?, created_by = ?, time_limit = ?, " +
            "max_attempts = ?, is_active = ?, start_time = ?, end_time = ? WHERE id = ?";
    private final static String FIND_BY_ID = "SELECT id, title, description, created_by, time_limit, max_attempts, is_active, start_time, end_time " +
            "FROM tests WHERE id = ?";
    private final static String FIND_ALL = "SELECT id, title, description, created_by, time_limit, max_attempts, is_active, start_time, end_time FROM tests";
    private final static String FIND_BY_CREATOR = "SELECT id, title, description, created_by, time_limit, max_attempts, is_active, start_time, end_time " +
            "FROM tests WHERE created_by = ?";
    private final static String FIND_ACTIVE = "SELECT id, title, description, created_by, time_limit, max_attempts, is_active, start_time, end_time" +
            "FROM tests WHERE is_active = true";
    private final static String DELETE = "DELETE FROM tests WHERE id = ?";

    private final Connection connection;

    public PostgresTestRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(Test test) {
        if (test.getId() == null) {
            return insert(test);
        } else {
            update(test);
            return test.getId();
        }
    }

    private Integer insert(Test test) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, test.getTitle());
            stmt.setString(2, test.getDescription());
            stmt.setInt(3, test.getCreatedBy());
            stmt.setInt(4, test.getTimeLimit());
            stmt.setInt(5, test.getMaxAttempts());
            stmt.setBoolean(6, test.isActive());
            stmt.setObject(7, test.getStartTime().toOffsetDateTime());
            stmt.setObject(8, test.getEndTime().toOffsetDateTime());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                test.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert test");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting test", e);
        }
    }

    private void update(Test test) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, test.getTitle());
            stmt.setString(2, test.getDescription());
            stmt.setInt(3, test.getCreatedBy());
            stmt.setInt(4, test.getTimeLimit());
            stmt.setInt(5, test.getMaxAttempts());
            stmt.setBoolean(6, test.isActive());
            stmt.setObject(7, test.getStartTime().toOffsetDateTime());
            stmt.setObject(8, test.getEndTime().toOffsetDateTime());
            stmt.setInt(9, test.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating test", e);
        }
    }

    @Override
    public Optional<Test> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToTest(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding test by id", e);
        }
    }

    @Override
    public List<Test> findAll() {
        List<Test> tests = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(FIND_ALL)) {
            while (rs.next()) {
                tests.add(mapResultSetToTest(rs));
            }
            return tests;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tests", e);
        }
    }

    @Override
    public List<Test> findByCreator(Integer creatorId) {
        List<Test> tests = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_CREATOR)) {
            stmt.setInt(1, creatorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tests.add(mapResultSetToTest(rs));
            }
            return tests;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tests by creator", e);
        }
    }

    @Override
    public List<Test> findActiveTests() {
        List<Test> tests = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(FIND_ACTIVE)) {
            while (rs.next()) {
                tests.add(mapResultSetToTest(rs));
            }
            return tests;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active tests", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting test", e);
        }
    }

    private Test mapResultSetToTest(ResultSet rs) throws SQLException {
        OffsetDateTime startTimeOdt = rs.getObject("start_time", OffsetDateTime.class);
        ZonedDateTime startTimeZdt = startTimeOdt.atZoneSameInstant(ZoneId.systemDefault());
        OffsetDateTime endTimeOdt = rs.getObject("end_time", OffsetDateTime.class);
        ZonedDateTime endTimeZdt = endTimeOdt.atZoneSameInstant(ZoneId.systemDefault());
        return new Test(
            rs.getInt("id"),
            rs.getInt("created_by"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getInt("time_limit"),
            rs.getInt("max_attempts"),
            rs.getBoolean("is_active"),
            startTimeZdt,
            endTimeZdt
        );
    }
}
