package DAO;

import Controlador.ClienteController;
import Modelo.Cliente;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RF06_EditarClienteTest {

    private final ClienteDAO dao = new ClienteDAO();

    @Test
    @DisplayName("CP01 - Modificacion valida de los datos de un cliente")
    void modificacionValida_seActualiza() {
        // ARRANGE
        String dni = dniUnico();
        dao.guardar(clienteValido(dni));
        Cliente c = dao.buscar(dni).get(0);
        c.setTelefono("999888777");
        c.setDireccion("Nueva direccion 456");

        // ACT
        boolean actualizado = dao.actualizar(c);

        // ASSERT
        assertTrue(actualizado);
        Cliente recargado = dao.buscar(dni).get(0);
        assertEquals("999888777", recargado.getTelefono());
        assertEquals("Nueva direccion 456", recargado.getDireccion());
    }

    @Test
    @DisplayName("CP02 - DNI ya en uso por otro cliente es detectado al editar")
    void dniYaEnUsoPorOtroCliente_esDetectado() {
        // ARRANGE: dos clientes distintos
        String dniA = dniUnico();
        dao.guardar(clienteValido(dniA));
        int idA = dao.buscar(dniA).get(0).getIdCliente();

        String dniB = dniUnico() + "1".substring(0, 0); // asegurar distinto
        dniB = dniUnico();
        dao.guardar(clienteValido(dniB));
        int idB = dao.buscar(dniB).get(0).getIdCliente();

        // ACT: el DNI de A ya esta en uso por alguien que no sea B?
        boolean conflicto = dao.existeDni(dniA, idB);

        // ASSERT
        assertTrue(conflicto, "El DNI de otro cliente debe detectarse como conflicto al editar B");
        // Control: el propio cliente A nunca debe chocar consigo mismo
        assertFalse(dao.existeDni(dniA, idA));
    }

    @Test
    @DisplayName("CP03 - Datos que no superan la validacion impiden la actualizacion")
    void datosNoSuperanValidacion_sonRechazados() {
        // ACT + ASSERT (mismo validador puro usado en el registro, RF04-CP03)
        assertNotNull(ClienteController.validarFormato("12345678", "Juan", "Perez", "abc", "", ""),
                "Telefono con letras debe rechazarse");
        assertNotNull(ClienteController.validarFormato("12345678", "J", "Perez", "", "", ""),
                "Nombre muy corto debe rechazarse");
    }

    private Cliente clienteValido(String dni) {
        Cliente c = new Cliente();
        c.setDni(dni);
        c.setNombres("ClienteQA");
        c.setApellidos("Edicion");
        c.setTelefono("987654321");
        c.setEmail("clienteqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setIdZona(0);
        c.setEstado(1);
        return c;
    }

    private String dniUnico() {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        long ms = System.nanoTime() % 100000000L;
        return String.format("%08d", ms);
    }
}
