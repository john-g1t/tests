package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.AnswerOption;
import com.github.john_g1t.domain.repository.AnswerOptionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresAnswerOptionRepository implements AnswerOptionRepository {
    private static final String INSERT = "INSERT INTO answer_options (question_id, option_text, score) " +
            "VALUES (?, ?, ?) RETURNING id";
    private static final String UPDATE = "UPDATE answer_options SET question_id = ?, option_text = ?, score = ? WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT id, question_id, option_text, score FROM answer_options WHERE id = ?";
    private static final String FIND_BY_QUESTION_ID = "SELECT id, question_id, option_text, score FROM answer_options WHERE question_id = ?";
    private static final String DELETE = "DELETE FROM answer_options WHERE id = ?";
    private final Connection connection;

    public PostgresAnswerOptionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(AnswerOption answerOption) {
        if (answerOption.getId() == null) {
            return insert(answerOption);
        } else {
            update(answerOption);
            return answerOption.getId();
        }
    }

    private Integer insert(AnswerOption answerOption) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, answerOption.getQuestionId());
            stmt.setString(2, answerOption.getOptionText());
            stmt.setInt(3, answerOption.getScore());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                answerOption.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert answer option");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting answer option", e);
        }
    }

    private void update(AnswerOption answerOption) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, answerOption.getQuestionId());
            stmt.setString(2, answerOption.getOptionText());
            stmt.setInt(3, answerOption.getScore());
            stmt.setInt(4, answerOption.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating answer option", e);
        }
    }

    @Override
    public Optional<AnswerOption> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToAnswerOption(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding answer option by id", e);
        }
    }

    @Override
    public List<AnswerOption> findByQuestionId(Integer questionId) {
        List<AnswerOption> options = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_QUESTION_ID)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                options.add(mapResultSetToAnswerOption(rs));
            }
            return options;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding answer options by question id", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting answer option", e);
        }
    }

    private AnswerOption mapResultSetToAnswerOption(ResultSet rs) throws SQLException {
        return new AnswerOption(
            rs.getInt("id"),
            rs.getInt("question_id"),
            rs.getString("option_text"),
            rs.getInt("score")
        );
    }
}
