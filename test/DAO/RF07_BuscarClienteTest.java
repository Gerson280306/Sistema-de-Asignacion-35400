package DAO;

import Modelo.Cliente;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF07 — Buscar cliente
 */
public class RF07_BuscarClienteTest {

    private final ClienteDAO dao = new ClienteDAO();
    private static final String MARCADOR = "ZqxBuscarQA";

    @BeforeAll
    static void crearClienteFixture() {
        ClienteDAO dao = new ClienteDAO();
        Cliente c = new Cliente();
        long valor = (System.nanoTime() + java.util.concurrent.ThreadLocalRandom.current().nextInt(1000)) % 100000000L;
        c.setDni(String.format("%08d", Math.abs(valor)));
        c.setNombres(MARCADOR);
        c.setApellidos("Fixture");
        c.setTelefono("987654321");
        c.setEmail("zqxbuscarqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setIdZona(0);
        c.setEstado(1);
        dao.guardar(c);
    }

    @Test
    @DisplayName("CP01 - Busqueda con texto que si tiene coincidencias")
    void busqueda_conCoincidencias() {
        // ACT
        List<Cliente> resultado = dao.buscar(MARCADOR);

        // ASSERT
        assertFalse(resultado.isEmpty(), "Debe encontrar al menos el cliente fixture");
        assertTrue(resultado.stream().anyMatch(c -> c.getNombres().equals(MARCADOR)));
    }

    @Test
    @DisplayName("CP02 - Busqueda sin coincidencias devuelve lista vacia")
    void busqueda_sinCoincidencias() {
        // ACT
        List<Cliente> resultado = dao.buscar("NoExisteEsteTextoEnNadieXYZ999");

        // ASSERT
        assertTrue(resultado.isEmpty());
    }

    @Test
    @Disabled("UI-only (navegacion: seleccionar una fila de la tabla de resultados y abrir "
            + "el formulario de edicion). Verificar manualmente.")
    @DisplayName("CP03 - Editar cliente desde el resultado de busqueda (evidencia manual)")
    void editarDesdeResultado_requiereEvidenciaManual() { }
}
