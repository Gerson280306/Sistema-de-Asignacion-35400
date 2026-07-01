package Controlador;

import DAO.UsuarioDAO;
import Modelo.Usuario;
import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF01 — Iniciar sesion
 * Cubre: CP01 (login valido), CP02 (campos vacios), CP03 (credenciales
 * incorrectas) y CP04 (bloqueo de cuenta tras 3 intentos fallidos).
 *
 * Clases bajo prueba: DAO.UsuarioDAO, Controlador.LoginController.camposVacios()
 */
public class RF01_IniciarSesionTest {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final String USUARIO = "qa_login_test";
    private static final String PASSWORD_OK = "Clave123";

    @BeforeAll
    static void crearUsuarioDePrueba() throws SQLException {
        UsuarioDAO dao = new UsuarioDAO();
        Usuario u = new Usuario();
        u.setUsername(USUARIO);
        u.setNombreCompleto("QA Login Test");
        u.setEmail("qa_login_test@gmail.com");
        u.setRol("SUPERVISOR");
        u.setEstado(1);
        dao.insertar(u, PASSWORD_OK); // si ya existe de una corrida previa, falla en silencio y no pasa nada
    }

    @BeforeEach
    void resetearEstadoDelUsuario() throws SQLException {
        // ARRANGE comun: siempre arrancar cada test con la cuenta activa y sin intentos fallidos
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(
                "UPDATE tb_usuario SET estado=1, intentos_fallidos=0 WHERE username=?")) {
            ps.setString(1, USUARIO);
            ps.executeUpdate();
        }
    }

    @Test
    @DisplayName("CP01 - Inicio de sesion valido devuelve el usuario con su rol")
    void loginValido_devuelveUsuarioConRolCorrecto() {
        // ACT
        Usuario resultado = usuarioDAO.autenticar(USUARIO, PASSWORD_OK);

        // ASSERT
        assertNotNull(resultado, "Con credenciales correctas no deberia devolver null");
        assertEquals(USUARIO, resultado.getUsername());
        assertEquals("SUPERVISOR", resultado.getRol());
    }

    @Test
    @DisplayName("CP02 - Campos vacios se detectan antes de consultar la base de datos")
    void camposVacios_detectaLosCuatroCasos() {
        // ACT + ASSERT
        assertTrue(Controlador.LoginController.camposVacios("", ""), "usuario y password vacios");
        assertTrue(Controlador.LoginController.camposVacios("", PASSWORD_OK), "solo usuario vacio");
        assertTrue(Controlador.LoginController.camposVacios(USUARIO, ""), "solo password vacio");
        assertFalse(Controlador.LoginController.camposVacios(USUARIO, PASSWORD_OK), "ambos llenos");
    }

    @Test
    @DisplayName("CP03 - Credenciales incorrectas devuelven null")
    void credencialesIncorrectas_devuelveNull() {
        // ACT
        Usuario resultado = usuarioDAO.autenticar(USUARIO, "ClaveIncorrecta999");

        // ASSERT
        assertNull(resultado, "Con password incorrecto no debe autenticar");
    }

    @Test
    @DisplayName("CP04 - Tres intentos fallidos consecutivos bloquean la cuenta")
    void tresIntentosFallidos_bloqueanLaCuenta() {
        // ACT: 3 intentos con password incorrecto
        assertNull(usuarioDAO.autenticar(USUARIO, "mala1"));
        assertNull(usuarioDAO.autenticar(USUARIO, "mala2"));
        assertNull(usuarioDAO.autenticar(USUARIO, "mala3"));

        // ASSERT: incluso con la contrasena CORRECTA, ya no debe poder entrar (cuenta bloqueada)
        Usuario resultado = usuarioDAO.autenticar(USUARIO, PASSWORD_OK);
        assertNull(resultado, "Tras 3 intentos fallidos la cuenta debe quedar bloqueada");
    }
}
