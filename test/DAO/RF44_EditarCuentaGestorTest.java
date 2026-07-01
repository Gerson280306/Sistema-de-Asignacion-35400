package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF44 - Editar cuenta de gestor
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF44_EditarCuentaGestorTest {

    private final UsuarioDAO dao = new UsuarioDAO();
    private static final String USERNAME = "qa_editar_gestor44";

    @BeforeAll
    static void crearGestor() {
        UsuarioDAO dao = new UsuarioDAO();
        if (!dao.usernameExiste(USERNAME, 0)) {
            Usuario u = new Usuario();
            u.setUsername(USERNAME);
            u.setNombreCompleto("Gestor Editar RF44");
            u.setEmail("gestoredit44@gmail.com");
            u.setRol("OPERADOR");
            u.setEstado(1);
            dao.insertar(u, "Clave123");
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP01 - Edicion correcta de nombre y email de un gestor")
    void cp01_edicionCorrecta_actualizaDatos() {
        // ARRANGE
        int idUsuario = obtenerIdPorUsername(USERNAME);
        assertTrue(idUsuario > 0);
        Usuario u = new Usuario();
        u.setIdUsuario(idUsuario);
        u.setUsername(USERNAME);
        u.setNombreCompleto("Gestor Editado RF44");
        u.setEmail("gestoredit44nuevo@gmail.com");
        u.setRol("SUPERVISOR");
        u.setEstado(1);

        // ACT
        boolean actualizado = dao.actualizar(u);

        // ASSERT
        assertTrue(actualizado);
        String nuevoNombre = obtenerNombrePorUsername(USERNAME);
        assertEquals("Gestor Editado RF44", nuevoNombre,
                "El nombre del gestor debe haberse actualizado");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Username ya en uso por otro usuario es detectado al editar")
    void cp02_usernameEnUsoPorOtro_esDetectado() {
        int idUsuario = obtenerIdPorUsername(USERNAME);
        assertTrue(idUsuario > 0);

        // "admin" pertenece a otro usuario
        boolean conflicto = dao.usernameExiste("admin", idUsuario);

        assertTrue(conflicto,
                "El username 'admin' esta en uso por otro usuario y debe detectarse como conflicto");
        // El propio username no debe conflictuar consigo mismo
        assertFalse(dao.usernameExiste(USERNAME, idUsuario),
                "El propio username no debe detectarse como conflicto al editarse a si mismo");
    }

    private int obtenerIdPorUsername(String username) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_usuario FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }

    private String obtenerNombrePorUsername(String username) {
        try (java.sql.PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT nombre_completo FROM tb_usuario WHERE username=?")) {
            ps.setString(1, username);
            java.sql.ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (Exception e) { return null; }
    }
}
