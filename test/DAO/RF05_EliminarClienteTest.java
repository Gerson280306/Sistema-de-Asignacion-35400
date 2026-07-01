package DAO;

import Modelo.Cliente;
import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class RF05_EliminarClienteTest {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final SolicitudDAO solicitudDAO = new SolicitudDAO();

    @Test
    @DisplayName("CP01 - Eliminacion valida de un cliente sin solicitudes activas")
    void eliminacionValida_clienteSinSolicitudes() {
        // ARRANGE
        String dni = dniUnico();
        clienteDAO.guardar(clienteValido(dni));
        int id = obtenerIdPorDni(dni);

        // ACT
        boolean tieneActivas = clienteDAO.tieneSolicitudesActivas(id);
        boolean eliminado = clienteDAO.eliminar(id);

        // ASSERT
        assertFalse(tieneActivas, "Un cliente recien creado no debe tener solicitudes activas");
        assertTrue(eliminado);
    }

    @Test
    @DisplayName("CP02 - No se permite eliminar un cliente con solicitudes activas")
    void clienteConSolicitudesActivas_noSePuedeEliminar() {
        // ARRANGE: cliente nuevo + una solicitud en estado ASIGNADA (activa)
        String dni = dniUnico();
        clienteDAO.guardar(clienteValido(dni));
        int idCliente = obtenerIdPorDni(dni);

        Solicitud s = new Solicitud();
        s.setIdCliente(idCliente);
        s.setIdTipoServicio(primerIdTipoServicio());
        s.setDescripcion("Servicio de prueba QA");
        s.setPrioridad("MEDIA");
        s.setEstado("ASIGNADA");
        solicitudDAO.guardar(s);

        // ACT
        boolean tieneActivas = clienteDAO.tieneSolicitudesActivas(idCliente);

        // ASSERT
        assertTrue(tieneActivas, "El sistema debe detectar que el cliente tiene una solicitud activa");
        // El controlador usa este resultado para bloquear el boton Eliminar (ver ClienteController.eliminarCliente()).
    }

    @Test
    @Disabled("UI-only (Alert de confirmacion). Verificar manualmente: clic en Eliminar -> "
            + "dialogo de confirmacion -> clic en No -> el cliente sigue activo en la tabla.")
    @DisplayName("CP03 - El gestor cancela la confirmacion (evidencia manual)")
    void gestorCancelaConfirmacion_requiereEvidenciaManual() { }

    private Cliente clienteValido(String dni) {
        Cliente c = new Cliente();
        c.setDni(dni);
        c.setNombres("ClienteQA");
        c.setApellidos("Eliminacion");
        c.setTelefono("987654321");
        c.setEmail("clienteqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setIdZona(0);
        c.setEstado(1);
        return c;
    }

    private static final java.util.concurrent.atomic.AtomicLong CONTADOR_DNI = new java.util.concurrent.atomic.AtomicLong();

    private String dniUnico() {
        long valor = (System.nanoTime() + CONTADOR_DNI.incrementAndGet()) % 100000000L;
        return String.format("%08d", Math.abs(valor));
    }

    private int obtenerIdPorDni(String dni) {
        var lista = clienteDAO.buscar(dni);
        assertFalse(lista.isEmpty(), "El cliente recien creado debe aparecer en la busqueda");
        return lista.get(0).getIdCliente();
    }

    private int primerIdTipoServicio() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
