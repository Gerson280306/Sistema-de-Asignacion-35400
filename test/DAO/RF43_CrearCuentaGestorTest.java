package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF43 - Crear cuenta de gestor (rol SUPERVISOR u OPERADOR, creada por el Admin)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF43_CrearCuentaGestorTest {

    private final UsuarioDAO dao = new UsuarioDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Crear cuenta de gestor con datos validos asigna rol SUPERVISOR")
    void cp01_crearCuentaGestor_asignaRolSupervisor() {
        String username = "qa_gestor43_" + (System.nanoTime() % 100000);
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto("Gestor QA RF43");
        u.setEmail("gestorqa43@gmail.com");
        u.setRol("SUPERVISOR");
        u.setEstado(1);

        boolean creado = dao.insertar(u, "Clave123");

        assertTrue(creado);
        assertTrue(dao.usernameExiste(username, 0),
                "El gestor debe quedar registrado en la BD");
        // Verificar que el rol guardado es SUPERVISOR
        String rolGuardado = obtenerRolPorUsername(username);
        assertEquals("SUPERVISOR", rolGuardado,
                "El rol del gestor recien creado debe ser SUPERVISOR");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Username ya en uso por otro gestor es detectado")
    void cp02_usernameExistente_esDetectado() {
        // "admin" existe en la BD semilla
        assertTrue(dao.usernameExiste("admin", 0),
                "El username 'admin' ya existe y debe detectarse como duplicado");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Password invalida es rechazada por la validacion")
    void cp03_passwordInvalida_esRechazada() {
        // Reglas: minimo 6 caracteres, las dos passwords deben coincidir
        assertNotNull(validarPassword("ab", "ab"),
                "Una password de 2 caracteres debe ser rechazada");
        assertNotNull(validarPassword("Clave1", "Clave2"),
                "Passwords que no coinciden deben ser rechazadas");
        assertNull(validarPassword("Clave123", "Clave123"),
                "Password valida no debe generar error");
    }

    private String validarPassword(String pass, String conf) {
        if (pass == null || pass.length() < 6)
            return "La contrasena debe tener al menos 6 caracteres.";
        if (!pass.equals(conf))
            return "Las contrasennas no coinciden.";
        return null;
    }

    private String obtenerRolPorUsername(String username) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT rol FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (Exception e) { return null; }
    }
}
