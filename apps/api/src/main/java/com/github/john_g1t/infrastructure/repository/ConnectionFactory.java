package com.github.john_g1t.infrastructure.repository;

import java.sql.Connection;

public interface ConnectionFactory {
    Connection getConnection();
    void closeConnection();
}
