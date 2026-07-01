package DAO;

import Modelo.Usuario;
import Conexion.ConexionDB;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class RF03_RecuperarPasswordTest {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final String USUARIO = "qa_recuperacion_test";
    private static final String EMAIL = "qa_recuperacion_test@gmail.com";
    private static final String PASSWORD_ORIGINAL = "ClaveOriginal1";

    @BeforeAll
    static void crearUsuarioDePrueba() {
        UsuarioDAO dao = new UsuarioDAO();
        Usuario u = new Usuario();
        u.setUsername(USUARIO);
        u.setNombreCompleto("QA Recuperacion Test");
        u.setEmail(EMAIL);
        u.setRol("OPERADOR");
        u.setEstado(1);
        dao.insertar(u, PASSWORD_ORIGINAL);
    }

    @Test
    @DisplayName("CP01 - Solicitud de enlace con correo valido genera un token utilizable")
    void solicitudEnlace_correoValido_generaToken() {
        // ACT
        String token = usuarioDAO.generarTokenRecuperacion(EMAIL);

        // ASSERT
        assertNotNull(token, "Debe generar un token para un correo registrado");
        assertFalse(token.isEmpty());
        assertTrue(usuarioDAO.validarToken(token), "El token recien generado debe ser valido");
    }

    @Test
    @DisplayName("CP02 - Cambio de contrasena con token valido la actualiza correctamente")
    void cambioPassword_tokenValido_actualizaPassword() {
        // ARRANGE
        String token = usuarioDAO.generarTokenRecuperacion(EMAIL);
        String nuevaPassword = "NuevaClave2024";

        // ACT
        boolean actualizado = usuarioDAO.actualizarPasswordConToken(token, nuevaPassword);

        // ASSERT
        assertTrue(actualizado);
        assertNotNull(usuarioDAO.autenticar(USUARIO, nuevaPassword),
                "Debe poder iniciar sesion con la nueva contrasena");
        assertFalse(usuarioDAO.validarToken(token), "El token ya usado no debe poder reutilizarse");
    }

    @Test
    @DisplayName("CP03 - Correo no registrado no genera token")
    void correoNoRegistrado_noGeneraToken() {
        // ACT
        String token = usuarioDAO.generarTokenRecuperacion("nadie_existe_xyz_999@correo.com");

        // ASSERT
        assertNull(token);
    }

    @Test
    @DisplayName("CP04 - Enlace ya vencido es rechazado como invalido")
    void enlaceExpirado_esRechazado() throws SQLException {
        // ARRANGE: insertamos directamente un token ya vencido
        // (el DAO no expone una forma de "crear vencido" porque en produccion nunca deberia pasar)
        String tokenVencido = "expirado_" + System.currentTimeMillis();
        int idUsuario = obtenerIdUsuario(USUARIO);
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(
                "INSERT INTO tb_recuperacion_password (id_usuario, token, fecha_expiracion) "
                        + "VALUES (?, ?, DATE_SUB(NOW(), INTERVAL 1 HOUR))")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tokenVencido);
            ps.executeUpdate();
        }

        // ACT + ASSERT
        assertFalse(usuarioDAO.validarToken(tokenVencido), "Un token vencido no debe ser valido");
        assertFalse(usuarioDAO.actualizarPasswordConToken(tokenVencido, "OtraClave123"),
                "No se debe poder cambiar la contrasena con un token vencido");
    }

    @Test
    @DisplayName("CP05 - Nueva contrasena que no cumple la politica es rechazada")
    void nuevaPasswordInvalida_esRechazada() {
        // ARRANGE
        String token = usuarioDAO.generarTokenRecuperacion(EMAIL);

        // ACT + ASSERT
        assertNotNull(UsuarioDAO.validarPoliticaPassword("abc"), "Muy corta y sin numero");
        assertFalse(usuarioDAO.actualizarPasswordConToken(token, "abc"),
                "No debe actualizar la contrasena si no cumple la politica minima");
    }

    private int obtenerIdUsuario(String username) throws SQLException {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(
                "SELECT id_usuario FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            var rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
}
