package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF46_ListarCuentasGestoresTest {

    private final UsuarioDAO dao = new UsuarioDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - listarGestores devuelve solo usuarios con rol SUPERVISOR u OPERADOR")
    void cp01_listarGestores_soloRolesGestor() {
        List<Usuario> gestores = dao.listarGestores();

        assertNotNull(gestores);
        assertFalse(gestores.isEmpty(),
                "Debe haber al menos un gestor en la BD (creados en tests RF33/RF43/RF44)");
        for (Usuario u : gestores) {
            assertTrue("SUPERVISOR".equals(u.getRol()) || "OPERADOR".equals(u.getRol()),
                    "listarGestores no debe devolver usuarios con rol ADMIN: id=" + u.getIdUsuario());
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - listarGestores no devuelve null aunque no haya gestores")
    void cp02_listarGestores_nuncaDevuelveNull() {
        List<Usuario> gestores = dao.listarGestores();

        assertNotNull(gestores,
                "listarGestores() nunca debe devolver null");
    }
}
