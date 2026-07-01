package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF34 - Cambiar password de cuenta
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF34_CambiarPasswordCuentaTest {

    private final UsuarioDAO dao = new UsuarioDAO();
    private static final String USUARIO = "qa_pwd_rf34";
    private static final String PASS_ORIGINAL = "ClaveOrig1";

    @BeforeAll
    static void crearUsuario() {
        UsuarioDAO dao = new UsuarioDAO();
        if (!dao.usernameExiste(USUARIO, 0)) {
            Usuario u = new Usuario();
            u.setUsername(USUARIO);
            u.setNombreCompleto("QA Password RF34");
            u.setRol("SUPERVISOR");
            u.setEstado(1);
            dao.insertar(u, PASS_ORIGINAL);
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP01 - Cambio de password valido permite autenticar con la nueva clave")
    void cp01_cambioPasswordValido_autenticaConNueva() {
        int idUsuario = obtenerIdPorUsername(USUARIO);
        String nuevaPass = "NuevaClave99";

        boolean cambiado = dao.cambiarPassword(idUsuario, nuevaPass);

        assertTrue(cambiado);
        assertNotNull(dao.autenticar(USUARIO, nuevaPass),
                "Debe poder autenticarse con la nueva password");
        assertNull(dao.autenticar(USUARIO, PASS_ORIGINAL),
                "La password original no debe funcionar despues del cambio");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Password muy corta (menos de 6 caracteres) es rechazada")
    void cp02_passwordMuyCorta_esRechazada() {
        assertNotNull(RF33_GestionarCuentaTest.validarDatosCuenta("u", "n", "abc", "abc"),
                "Una password de 3 caracteres debe ser rechazada por la validacion");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Passwords que no coinciden son rechazadas")
    void cp03_passwordsNoCoinciden_sonRechazadas() {
        assertNotNull(RF33_GestionarCuentaTest.validarDatosCuenta("u", "n", "Clave1", "Clave2"),
                "Passwords distintas deben ser rechazadas por la validacion");
    }

    private int obtenerIdPorUsername(String username) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_usuario FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }
}
