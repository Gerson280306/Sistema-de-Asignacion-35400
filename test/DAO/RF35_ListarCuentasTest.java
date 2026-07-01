package DAO;

import Modelo.Usuario;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF35 - Listar cuentas de usuario
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF35_ListarCuentasTest {

    private final UsuarioDAO dao = new UsuarioDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - listarTodos devuelve todas las cuentas registradas")
    void cp01_listarTodos_devuelveCuentas() {
        List<Usuario> lista = dao.listarTodos();

        assertNotNull(lista);
        assertFalse(lista.isEmpty(),
                "Ya hay usuarios en la BD semilla, la lista no debe estar vacia");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - listarGestores devuelve solo SUPERVISOR y OPERADOR (no ADMIN)")
    void cp02_listarGestores_noIncluyeAdmin() {
        List<Usuario> gestores = dao.listarGestores();

        assertNotNull(gestores);
        for (Usuario u : gestores) {
            assertNotEquals("ADMIN", u.getRol(),
                    "listarGestores() no debe devolver cuentas con rol ADMIN");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - usernameExiste devuelve true para username existente")
    void cp03_usernameExiste_trueParaExistente() {
        assertTrue(dao.usernameExiste("admin", 0),
                "El username 'admin' existe en la BD semilla");
        assertFalse(dao.usernameExiste("usuario_que_no_existe_xyz999", 0),
                "Un username inexistente debe devolver false");
    }
}
