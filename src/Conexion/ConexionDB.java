package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String HOST     = "localhost";
    private static final String PUERTO   = "3306";
    private static final String BASE     = "db_asignacion";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "S0p0rt3";  
    private static final String URL      =
        "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BASE
        + "?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true";

    private static ConexionDB instancia;
    private Connection conexion;

    private ConexionDB() {
        conectar();
    }

    public static ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    private void conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("[DB] Conexion establecida con " + BASE);
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL no encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Error al conectar: " + e.getMessage());
        }
    }

    public Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed()) {
                System.out.println("[DB] Reconectando...");
                conectar();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al verificar conexión: " + e.getMessage());
        }
        return conexion;
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("[DB] Conexion cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error al cerrar conexión: " + e.getMessage());
        }
    }
}
