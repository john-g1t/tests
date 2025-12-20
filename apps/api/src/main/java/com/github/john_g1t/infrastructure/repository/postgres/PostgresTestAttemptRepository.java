package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.TestAttempt;
import com.github.john_g1t.domain.repository.TestAttemptRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresTestAttemptRepository implements TestAttemptRepository {
    private final static String INSERT = "INSERT INTO test_attempts (user_id, test_id, start_time, end_time, score, attempt_number) " +
            "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
    private final static String UPDATE = "UPDATE test_attempts SET user_id = ?, test_id = ?, start_time = ?, end_time = ?, " +
            "score = ?, attempt_number = ? WHERE id = ?";
    private final static String FIND_BY_ID = "SELECT id, user_id, test_id, start_time, end_time, score, attempt_number FROM test_attempts WHERE id = ?";
    private final static String FIND_BY_USER_ID = "SELECT id, user_id, test_id, start_time, end_time, score, attempt_number FROM test_attempts WHERE user_id = ?";
    private final static String FIND_BY_TEST_ID = "SELECT id, user_id, test_id, start_time, end_time, score, attempt_number FROM test_attempts WHERE test_id = ?";
    private final static String FIND_BY_USER_AND_TEST = "SELECT id, user_id, test_id, start_time, end_time, score, attempt_number " +
            "FROM test_attempts WHERE user_id = ? AND test_id = ?";
    private final static String DELETE = "DELETE FROM test_attempts WHERE id = ?";

    private final Connection connection;

    public PostgresTestAttemptRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(TestAttempt attempt) {
        if (attempt.getId() == null) {
            return insert(attempt);
        } else {
            update(attempt);
            return attempt.getId();
        }
    }

    private Integer insert(TestAttempt attempt) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, attempt.getUserId());
            stmt.setInt(2, attempt.getTestId());
            stmt.setObject(3, attempt.getStartTime().toOffsetDateTime());
            stmt.setObject(4, attempt.getEndTime().toOffsetDateTime());
            stmt.setInt(5, attempt.getScore());
            stmt.setInt(6, attempt.getAttemptNumber());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                attempt.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert test attempt");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting test attempt", e);
        }
    }

    private void update(TestAttempt attempt) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, attempt.getUserId());
            stmt.setInt(2, attempt.getTestId());
            stmt.setObject(3, attempt.getStartTime().toOffsetDateTime());
            stmt.setObject(4, attempt.getEndTime().toOffsetDateTime());
            stmt.setInt(5, attempt.getScore());
            stmt.setInt(6, attempt.getAttemptNumber());
            stmt.setInt(7, attempt.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating test attempt", e);
        }
    }

    @Override
    public Optional<TestAttempt> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToTestAttempt(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding test attempt by id", e);
        }
    }

    @Override
    public List<TestAttempt> findByUserId(Integer userId) {
        List<TestAttempt> attempts = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_USER_ID)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attempts.add(mapResultSetToTestAttempt(rs));
            }
            return attempts;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding test attempts by user id", e);
        }
    }

    @Override
    public List<TestAttempt> findByTestId(Integer testId) {
        List<TestAttempt> attempts = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_TEST_ID)) {
            stmt.setInt(1, testId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attempts.add(mapResultSetToTestAttempt(rs));
            }
            return attempts;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding test attempts by test id", e);
        }
    }

    @Override
    public List<TestAttempt> findByUserAndTest(Integer userId, Integer testId) {
        List<TestAttempt> attempts = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_USER_AND_TEST)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, testId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                attempts.add(mapResultSetToTestAttempt(rs));
            }
            return attempts;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding test attempts by user and test", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting test attempt", e);
        }
    }

    private TestAttempt mapResultSetToTestAttempt(ResultSet rs) throws SQLException {
        OffsetDateTime startTimeOdt = rs.getObject("start_time", OffsetDateTime.class);
        ZonedDateTime startTimeZdt = startTimeOdt.atZoneSameInstant(ZoneId.systemDefault());
        OffsetDateTime endTimeOdt = rs.getObject("end_time", OffsetDateTime.class);
        ZonedDateTime endTimeZdt = endTimeOdt.atZoneSameInstant(ZoneId.systemDefault());
        return new TestAttempt(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("test_id"),
            startTimeZdt,
            endTimeZdt,
            rs.getInt("score"),
            rs.getInt("attempt_number")
        );
    }
}