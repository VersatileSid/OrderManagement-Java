package com.example.librarymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";

    private static final String USER = "root";

    private static final String PASSWORD = "Siddharth@123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {

        try {
            Connection connection = getConnection();

            System.out.println("MySQL connection successful!");

            connection.close();

        } catch (SQLException e) {

            System.out.println("MySQL connection failed!");
            e.printStackTrace();
        }
    }
}