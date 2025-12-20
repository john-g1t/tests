package com.github.john_g1t.infrastructure.repository.postgres;

import com.github.john_g1t.domain.model.User;
import com.github.john_g1t.domain.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresUserRepository implements UserRepository {
    private static final String INSERT =
            "INSERT INTO users (email, password, first_name, last_name) VALUES (?, ?, ?, ?) RETURNING id";
    private static final String UPDATE =
            "UPDATE users SET email = ?, password = ?, first_name = ?, last_name = ? WHERE id = ?";
    private static final String FIND_BY_ID =
            "SELECT id, email, password, first_name, last_name FROM users WHERE id = ?";
    private static final String FIND_BY_EMAIL =
            "SELECT id, email, password, first_name, last_name FROM users WHERE email = ?";
    private static final String FIND_ALL =
            "SELECT id, email, password, first_name, last_name FROM users";
    private static final String EXISTS_BY_EMAIL =
            "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String EXISTS_BY_ID =
            "SELECT COUNT(*) FROM users WHERE id = ?";
    private static final String DELETE_BY_ID =
            "DELETE FROM users WHERE id = ?";

    private final Connection connection;

    public PostgresUserRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Integer save(User user) {
        if (user.getId() == null) {
            return insert(user);
        } else {
            update(user);
            return user.getId();
        }
    }

    private Integer insert(User user) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(1);
                user.setId(id);
                return id;
            }
            throw new RuntimeException("Failed to insert user");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user", e);
        }
    }

    private void update(User user) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setInt(5, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public Optional<User> findById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement(FIND_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(FIND_ALL)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence by email", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(EXISTS_BY_ID)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking user existence by id", e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_ID)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("first_name"),
            rs.getString("last_name")
        );
    }
}
