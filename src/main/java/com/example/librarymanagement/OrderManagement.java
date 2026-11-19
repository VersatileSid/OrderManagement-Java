
package com.example.librarymanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class OrderManagement {

    // Q1(a) - Insert Order
    public static void insertOrder(Order order) {

        String sql = """
                INSERT INTO Orders
                (order_id, customer_name, product_name, quantity, unit_price, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, order.getOrderId());
            statement.setString(2, order.getCustomerName());
            statement.setString(3, order.getProductName());
            statement.setInt(4, order.getQuantity());
            statement.setDouble(5, order.getUnitPrice());
            statement.setString(6, order.getStatus());

            statement.executeUpdate();

            System.out.println("Order inserted successfully.");

        } catch (SQLException e) {

            if (e.getErrorCode() == 1062) {
                System.out.println("Error: Order ID already exists.");
            } else {
                System.out.println("Error while inserting order.");
                e.printStackTrace();
            }
        }
    }


    // Q1(b) - Display orders by status and quantity > 2
    public static void displayOrdersByStatus() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter order status: ");
        String status = scanner.nextLine();

        String sql = """
                SELECT order_id, customer_name, product_name,
                       quantity, unit_price, status
                FROM Orders
                WHERE status = ? AND quantity > 2
                ORDER BY unit_price DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);

            ResultSet resultSet = statement.executeQuery();

            boolean found = false;

            while (resultSet.next()) {

                found = true;

                System.out.println("--------------------------------");
                System.out.println("Order ID: "
                        + resultSet.getInt("order_id"));

                System.out.println("Customer: "
                        + resultSet.getString("customer_name"));

                System.out.println("Product: "
                        + resultSet.getString("product_name"));

                System.out.println("Quantity: "
                        + resultSet.getInt("quantity"));

                System.out.println("Unit Price: "
                        + resultSet.getDouble("unit_price"));

                System.out.println("Status: "
                        + resultSet.getString("status"));
            }

            if (!found) {
                System.out.println(
                        "No orders found with status '" + status
                                + "' and quantity greater than 2."
                );
            }

            resultSet.close();

        } catch (SQLException e) {

            System.out.println("Error while retrieving orders.");
            e.printStackTrace();
        }
    }


    // Q1(c) - Increase quantity
    public static void increaseQuantity() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();

        System.out.print("Enter additional quantity: ");
        int additionalQuantity = scanner.nextInt();

        String checkSql = """
                SELECT quantity
                FROM Orders
                WHERE order_id = ?
                """;

        String updateSql = """
                UPDATE Orders
                SET quantity = quantity + ?
                WHERE order_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSql)) {

            checkStatement.setInt(1, orderId);

            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {

                int oldQuantity = resultSet.getInt("quantity");

                try (PreparedStatement updateStatement =
                             connection.prepareStatement(updateSql)) {

                    updateStatement.setInt(1, additionalQuantity);
                    updateStatement.setInt(2, orderId);

                    updateStatement.executeUpdate();

                    int newQuantity = oldQuantity + additionalQuantity;

                    System.out.println("Quantity updated successfully.");
                    System.out.println("Order ID: " + orderId);
                    System.out.println("Old Quantity: " + oldQuantity);
                    System.out.println("Additional Quantity: "
                            + additionalQuantity);
                    System.out.println("New Quantity: " + newQuantity);
                }

            } else {

                System.out.println(
                        "Order with ID " + orderId + " does not exist."
                );
            }

            resultSet.close();

        } catch (SQLException e) {

            System.out.println("Error while updating quantity.");
            e.printStackTrace();
        }
    }


    // Q1(d) - Update order status
    public static void updateOrderStatus() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter new status: ");
        String newStatus = scanner.nextLine();

        // Validate status
        if (!newStatus.equalsIgnoreCase("Pending")
                && !newStatus.equalsIgnoreCase("Shipped")
                && !newStatus.equalsIgnoreCase("Delivered")) {

            System.out.println(
                    "Invalid status. Valid statuses are: "
                            + "Pending, Shipped, Delivered"
            );

            return;
        }

        String selectSql = """
                SELECT order_id, customer_name, product_name,
                       quantity, unit_price, status
                FROM Orders
                WHERE order_id = ?
                """;

        String updateSql = """
                UPDATE Orders
                SET status = ?
                WHERE order_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement selectStatement =
                     connection.prepareStatement(selectSql)) {

            // Check whether order exists
            selectStatement.setInt(1, orderId);

            ResultSet resultSet = selectStatement.executeQuery();

            if (!resultSet.next()) {

                System.out.println(
                        "Order with ID " + orderId + " does not exist."
                );

                return;
            }

            // Update status
            try (PreparedStatement updateStatement =
                         connection.prepareStatement(updateSql)) {

                updateStatement.setString(1, newStatus);
                updateStatement.setInt(2, orderId);

                updateStatement.executeUpdate();
            }

            // Display updated details
            System.out.println("\nOrder status updated successfully.");
            System.out.println("--------------------------------");

            System.out.println("Order ID: "
                    + resultSet.getInt("order_id"));

            System.out.println("Customer: "
                    + resultSet.getString("customer_name"));

            System.out.println("Product: "
                    + resultSet.getString("product_name"));

            System.out.println("Quantity: "
                    + resultSet.getInt("quantity"));

            System.out.println("Unit Price: "
                    + resultSet.getDouble("unit_price"));

            System.out.println("New Status: " + newStatus);

            resultSet.close();

        } catch (SQLException e) {

            System.out.println("Error while updating order status.");
            e.printStackTrace();
        }
    }


    // Q1(e) - Display total order value and delete Cancelled order
    public static void displayTotalAndDeleteCancelledOrder() {

        Scanner scanner = new Scanner(System.in);

        // Step 1: Calculate total order value
        String sumSql = """
                SELECT SUM(quantity * unit_price) AS total_order_value
                FROM Orders
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sumSql)) {

            if (resultSet.next()) {
                double totalOrderValue =
                        resultSet.getDouble("total_order_value");

                System.out.println("\nTotal Order Value: "
                        + totalOrderValue);
            }

        } catch (SQLException e) {
            System.out.println("Error calculating total order value.");
            e.printStackTrace();
            return;
        }

        // Step 2: Accept order ID
        System.out.print("\nEnter Order ID to delete: ");
        int orderId = scanner.nextInt();

        // Step 3: Check order status
        String checkSql = """
                SELECT status
                FROM Orders
                WHERE order_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSql)) {

            checkStatement.setInt(1, orderId);

            ResultSet checkResult = checkStatement.executeQuery();

            if (checkResult.next()) {

                String status = checkResult.getString("status");

                // Step 4: Delete only if status is Cancelled
                if (status.equalsIgnoreCase("Cancelled")) {

                    String deleteSql = """
                            DELETE FROM Orders
                            WHERE order_id = ? AND status = 'Cancelled'
                            """;

                    try (PreparedStatement deleteStatement =
                                 connection.prepareStatement(deleteSql)) {

                        deleteStatement.setInt(1, orderId);

                        int rowsDeleted = deleteStatement.executeUpdate();

                        if (rowsDeleted > 0) {
                            System.out.println(
                                    "Cancelled order deleted successfully."
                            );
                        } else {
                            System.out.println(
                                    "Order could not be deleted."
                            );
                        }
                    }

                } else {

                    System.out.println(
                            "Order cannot be deleted because its status is "
                                    + status + ". Only Cancelled orders "
                                    + "can be deleted."
                    );
                }

            } else {

                System.out.println(
                        "Order with ID " + orderId + " does not exist."
                );
            }

            checkResult.close();

        } catch (SQLException e) {

            System.out.println("Error while deleting order.");
            e.printStackTrace();
        }
    }


    // Main method
    public static void main(String[] args) {

        /*
         * Q1(a)
         * Orders have already been inserted.
         */

        // Order order1 = new Order(
        //         101, "Rahul", "Laptop",
        //         2, 55000.00, "Pending");

        // Order order2 = new Order(
        //         102, "Priya", "Mobile",
        //         3, 25000.00, "Shipped");

        // Order order3 = new Order(
        //         103, "Amit", "Headphones",
        //         4, 2000.00, "Delivered");

        // insertOrder(order1);
        // insertOrder(order2);
        // insertOrder(order3);


        /*
         * Q1(b)
         */

        // displayOrdersByStatus();


        /*
         * Q1(c)
         */

        // increaseQuantity();


        /*
         * Q1(d)
         */

        // updateOrderStatus();


        /*
         * Q1(e)
         */

        displayTotalAndDeleteCancelledOrder();
    }
}
