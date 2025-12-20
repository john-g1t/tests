package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.UserAnswer;
import com.github.john_g1t.domain.repository.UserAnswerRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresUserAnswerRepository implements UserAnswerRepository {
    private static final String INSERT = "INSERT INTO user_answers (attempt_id, question_id, answer_id, answer_text) " +
            "VALUES (?, ?, ?, ?) RETURNING id";
    private static final String UPDATE = "UPDATE user_answers SET attempt_id = ?, question_id = ?, answer_id = ?, " +
            "answer_text = ?, WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT id, attempt_id, question_id, answer_id, answer_text " +
            "FROM user_answers WHERE id = ?";
    private static final String FIND_BY_ATTEMPT_ID = "SELECT id, attempt_id, question_id, answer_id, answer_text " +
            "FROM user_answers WHERE attempt_id = ?";
    private static final String DELETE = "DELETE FROM user_answers WHERE id = ?";

    private final Connection connection;

    public PostgresUserAnswerRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(UserAnswer answer) {
        if (answer.getId() == null) {
            return insert(answer);
        } else {
            update(answer);
            return answer.getId();
        }
    }

    private Integer insert(UserAnswer answer) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, answer.getAttemptId());
            stmt.setInt(2, answer.getQuestionId());
            stmt.setInt(3, answer.getAnswerId());
            stmt.setString(4, answer.getAnswerText());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                answer.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert user answer");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user answer", e);
        }
    }

    private void update(UserAnswer answer) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, answer.getAttemptId());
            stmt.setInt(2, answer.getQuestionId());
            stmt.setInt(3, answer.getAnswerId());
            stmt.setString(4, answer.getAnswerText());
            stmt.setInt(5, answer.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user answer", e);
        }
    }

    @Override
    public Optional<UserAnswer> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUserAnswer(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user answer by id", e);
        }
    }

    @Override
    public List<UserAnswer> findByAttemptId(Integer attemptId) {
        List<UserAnswer> answers = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ATTEMPT_ID)) {
            stmt.setInt(1, attemptId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                answers.add(mapResultSetToUserAnswer(rs));
            }
            return answers;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user answers by attempt id", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user answer", e);
        }
    }

    private UserAnswer mapResultSetToUserAnswer(ResultSet rs) throws SQLException {
        return new UserAnswer(
            rs.getInt("id"),
            rs.getInt("attempt_id"),
            rs.getInt("question_id"),
            rs.getInt("answer_id"),
            rs.getString("answer_text")
        );
    }
}
