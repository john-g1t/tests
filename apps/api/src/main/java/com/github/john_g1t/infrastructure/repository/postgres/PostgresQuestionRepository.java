package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.Question;
import com.github.john_g1t.domain.repository.QuestionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresQuestionRepository implements QuestionRepository {
    private static final String INSERT = "INSERT INTO questions (test_id, text, answer_type, max_points) " +
            "VALUES (?, ?, ?::answer_type, ?) RETURNING id";
    private static final String UPDATE = "UPDATE questions SET test_id = ?, text = ?, answer_type = ?, max_points = ? WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT id, test_id, text, answer_type, max_points FROM questions WHERE id = ?";
    private static final String FIND_BY_TEST_ID = "SELECT id, test_id, text, answer_type, max_points FROM questions WHERE test_id = ?";
    private static final String DELETE = "DELETE FROM questions WHERE id = ?";
    private final Connection connection;

    public PostgresQuestionRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(Question question) {
        if (question.getId() == null) {
            return insert(question);
        } else {
            update(question);
            return question.getId();
        }
    }

    private Integer insert(Question question) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setInt(1, question.getTestId());
            stmt.setString(2, question.getText());
            stmt.setString(3, question.getAnswerType());
            stmt.setInt(4, question.getMaxPoints());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                question.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert question");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting question", e);
        }
    }

    private void update(Question question) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setInt(1, question.getTestId());
            stmt.setString(2, question.getText());
            stmt.setString(3, question.getAnswerType());
            stmt.setInt(4, question.getMaxPoints());
            stmt.setInt(5, question.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating question", e);
        }
    }

    @Override
    public Optional<Question> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToQuestion(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding question by id", e);
        }
    }

    @Override
    public List<Question> findByTestId(Integer testId) {
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_TEST_ID)) {
            stmt.setInt(1, testId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                questions.add(mapResultSetToQuestion(rs));
            }
            return questions;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding questions by test id", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting question", e);
        }
    }

    private Question mapResultSetToQuestion(ResultSet rs) throws SQLException {
        return new Question(
            rs.getInt("id"),
            rs.getInt("test_id"),
            rs.getString("text"),
            rs.getString("answer_type"),
            rs.getInt("max_points")
        );
    }
}
