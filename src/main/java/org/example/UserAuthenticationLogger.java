package org.example;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAuthenticationLogger {

    // Método para registrar la autenticación del usuario
    public void logUserAuthentication(String username) {
        // Configuración de la variable de entorno JDBC_DATABASE_URL
        String url = "jdbc:postgresql://comet-ox-7099.g8z.gcp-us-east1.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full&sslrootcert=" + getResourcePath("/certificados/root.crt");
        System.setProperty("JDBC_DATABASE_URL", url);

        // Resto del código sin cambios
        String user = "vianny"; // Reemplaza "your_username" con tu nombre de usuario real
        String password = "HLuEHW5PXoZ8GQfMCwbvHQ"; // Reemplaza "your_password" con tu contraseña real

        // SQL para crear la tabla si no existe
        String createTableSQL = "CREATE TABLE IF NOT EXISTS user_authentication_logs (id BIGSERIAL PRIMARY KEY, username VARCHAR(255), login_time TIMESTAMP)";

        // Sentencia SQL para insertar la fecha, hora y usuario autenticado
        String sql = "INSERT INTO user_authentication_logs (username, login_time) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement createStmt = conn.prepareStatement(createTableSQL);
             PreparedStatement insertStmt = conn.prepareStatement(sql)) {

            // Crear la tabla si no existe
            createStmt.executeUpdate();

            // Configuración de los parámetros de la sentencia SQL
            insertStmt.setString(1, username);
            insertStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            // Ejecución de la sentencia SQL para insertar registros
            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener todos los usuarios autenticados con todos sus datos
    public List<UserData> getAllAuthenticatedUsers() {
        List<UserData> users = new ArrayList<>();
        // Obtener la URL de conexión JDBC de la variable de entorno
        String url = System.getProperty("JDBC_DATABASE_URL");

        // Validar que la URL de conexión no sea nula
        if (url == null || url.isEmpty()) {
            System.err.println("La variable de entorno JDBC_DATABASE_URL no está configurada.");
            return users;
        }

        String user = "vianny";
        String password = "HLuEHW5PXoZ8GQfMCwbvHQ";
        String sql = "SELECT * FROM user_authentication_logs";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Iterar sobre el conjunto de resultados y obtener los usuarios
            while (rs.next()) {
                long id = rs.getLong("id");
                String username = rs.getString("username");
                Timestamp loginTime = rs.getTimestamp("login_time");
                UserData userData = new UserData(id, username, loginTime);
                users.add(userData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    // Método para imprimir todos los datos de cada usuario en pantalla
    public void printAllAuthenticatedUsers() {
        List<UserData> users = getAllAuthenticatedUsers();

        if (users.isEmpty()) {
            System.out.println("No hay usuarios autenticados.");
        } else {
            System.out.println("Usuarios autenticados:");
            for (UserData user : users) {
                System.out.println("ID: " + user.getId());
                System.out.println("Username: " + user.getUsername());
                System.out.println("Login Time: " + user.getLoginTime());
                System.out.println("---------------------------------------");
            }
        }
    }

    // Método para obtener la ruta del recurso dentro de la carpeta de certificados
    private String getResourcePath(String resourceName) {
        URL resourceUrl = getClass().getResource(resourceName);
        return resourceUrl != null ? resourceUrl.getPath() : "";
    }

    // Clase interna para almacenar los datos de cada usuario
    static class UserData {
        private long id;
        private String username;
        private Timestamp loginTime;

        public UserData(long id, String username, Timestamp loginTime) {
            this.id = id;
            this.username = username;
            this.loginTime = loginTime;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public Timestamp getLoginTime() {
            return loginTime;
        }
    }
}
