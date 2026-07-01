package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF33_GestionarCuentaTest {

    private final UsuarioDAO dao = new UsuarioDAO();

    public static String validarDatosCuenta(String username, String nombre,
                                             String pass, String passConf) {
        if (username == null || username.trim().isEmpty())
            return "Usuario y nombre son obligatorios.";
        if (nombre == null || nombre.trim().isEmpty())
            return "Usuario y nombre son obligatorios.";
        if (pass != null && !pass.isEmpty()) {
            if (pass.length() < 6)
                return "La contrasena debe tener al menos 6 caracteres.";
            if (!pass.equals(passConf))
                return "Las contrasennas no coinciden.";
        }
        return null;
    }

    @Test
    @Order(1)
    @DisplayName("CP01 - Crear cuenta nueva con datos validos")
    void cp01_crearCuentaValida() {
        String username = "qa_cuenta_" + (System.nanoTime() % 100000);
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto("QA Cuenta RF33");
        u.setEmail("qacuenta33@gmail.com");
        u.setRol("SUPERVISOR");
        u.setEstado(1);

        boolean creado = dao.insertar(u, "Clave123");

        assertTrue(creado, "La cuenta con datos validos debe crearse");
        assertFalse(dao.usernameExiste(username, 0) == false,
                "El username debe existir despues de crearse");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Username duplicado es rechazado")
    void cp02_usernameDuplicado_esRechazado() {
        boolean duplicado = dao.usernameExiste("admin", 0);

        assertTrue(duplicado, "El username 'admin' ya existe en la BD semilla");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Datos invalidos son detectados por la validacion antes de llegar a la BD")
    void cp03_datosInvalidos_sonDetectados() {
        assertNotNull(validarDatosCuenta("", "Juan", "Clave1", "Clave1"),
                "Username vacio debe rechazarse");
        assertNotNull(validarDatosCuenta("juan", "", "Clave1", "Clave1"),
                "Nombre vacio debe rechazarse");
        assertNotNull(validarDatosCuenta("juan", "Juan", "abc", "abc"),
                "Password de menos de 6 caracteres debe rechazarse");
        assertNotNull(validarDatosCuenta("juan", "Juan", "Clave1", "Clave2"),
                "Passwords que no coinciden deben rechazarse");
        assertNull(validarDatosCuenta("juan", "Juan", "Clave123", "Clave123"),
                "Datos validos no deben generar error");
    }

    @Test
    @Order(4)
    @DisplayName("CP04 - Desactivar y reactivar una cuenta cambia su estado correctamente")
    void cp04_toggleEstado_cambiaEstado() {
        String username = "qa_toggle_" + (System.nanoTime() % 100000);
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto("QA Toggle RF33");
        u.setRol("SUPERVISOR");
        u.setEstado(1);
        dao.insertar(u, "Clave123");

        int idUsuario = obtenerIdPorUsername(username);
        assertTrue(idUsuario > 0);

        boolean desactivado = dao.toggleEstado(idUsuario, 0);
        assertTrue(desactivado);
        assertEquals(0, obtenerEstado(idUsuario), "Debe estar desactivado");

        boolean reactivado = dao.toggleEstado(idUsuario, 1);
        assertTrue(reactivado);
        assertEquals(1, obtenerEstado(idUsuario), "Debe estar reactivado");
    }

    private int obtenerIdPorUsername(String username) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_usuario FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }

    private int obtenerEstado(int idUsuario) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT estado FROM tb_usuario WHERE id_usuario=?")) {
            ps.setInt(1, idUsuario);
            java.sql.ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getInt(1);
        } catch (Exception e) { return -1; }
    }
}
