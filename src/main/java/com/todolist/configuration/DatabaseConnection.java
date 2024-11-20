package com.todolist.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/todolist";
    private static final String USER = "todolist_user";
    private static final String PASSWORD = "todolist_password";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver"); // Registrar o driver manualmente
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão bem-sucedida com o PostgreSQL!");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC não encontrado: " + e.getMessage());
            throw new SQLException("Driver JDBC não encontrado.", e);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
            throw new SQLException("Erro ao conectar.", e);
        }
    }
}
