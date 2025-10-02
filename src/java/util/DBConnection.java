package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private Connection con;

    public DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3308/micasita", "root", ""
            );
            System.out.println("Conexión exitosa a la BD");
        } catch (Exception e) {
            System.err.println("Error en la conexión: " + e);
        }
    }

    // Método de instancia
    public Connection getConnection() {
        return con;
    }

    // Método estático (más práctico en servlets/services)
    public static Connection getConnectionStatic() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3308/micasita", "root", ""
            );
        } catch (Exception e) {
            throw new RuntimeException("Error en la conexión: " + e);
        }
    }
}

