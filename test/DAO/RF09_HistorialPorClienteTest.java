package DAO;

import Modelo.Cliente;
import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RF09_HistorialPorClienteTest {

    private final SolicitudDAO solicitudDAO = new SolicitudDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Test
    @DisplayName("CP01 - Carga el historial de un cliente con solicitudes")
    void cargaHistorial_clienteConSolicitudes() {
        // ARRANGE: cliente fixture + 1 solicitud asociada
        String marcador = "ZqxHistorialQA";
        Cliente c = new Cliente();
        c.setDni(dniUnico());
        c.setNombres(marcador);
        c.setApellidos("Fixture");
        c.setTelefono("987654321");
        c.setEmail("zqxhistorialqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setIdZona(0);
        c.setEstado(1);
        clienteDAO.guardar(c);
        int idCliente = clienteDAO.buscar(marcador).get(0).getIdCliente();

        Solicitud s = new Solicitud();
        s.setIdCliente(idCliente);
        s.setIdTipoServicio(primerIdTipoServicio());
        s.setDescripcion("Servicio historico QA");
        s.setPrioridad("BAJA");
        s.setEstado("COMPLETADA");
        solicitudDAO.guardar(s);

        // ACT
        List<Solicitud> historial = solicitudDAO.buscar(marcador);

        // ASSERT
        assertFalse(historial.isEmpty(), "Debe traer al menos la solicitud recien creada");
        assertEquals(marcador + " Fixture", historial.get(0).getNombreCliente().trim());
    }

    @Test
    @Disabled("HistorialController.filtrar() esta sin implementar (TODO en el codigo fuente). "
            + "SolicitudDAO tampoco expone todavia un filtro combinado por estado + rango de "
            + "fechas. Es una funcionalidad pendiente real del proyecto, no alcanza para esta sesion.")
    @DisplayName("CP02 - Filtrado de historial por estado y fecha (funcionalidad pendiente)")
    void filtradoPorEstadoYFecha_pendiente() { }

    @Test
    @DisplayName("CP03 - Cliente sin solicitudes muestra historial vacio")
    void clienteSinSolicitudes_historialVacio() {
        // ARRANGE: cliente fixture sin ninguna solicitud asociada
        String marcador = "ZqxSinHistorialQA";
        Cliente c = new Cliente();
        c.setDni(dniUnico());
        c.setNombres(marcador);
        c.setApellidos("Fixture");
        c.setTelefono("987654321");
        c.setEmail("zqxsinhistorialqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setIdZona(0);
        c.setEstado(1);
        clienteDAO.guardar(c);

        // ACT
        List<Solicitud> historial = solicitudDAO.buscar(marcador);

        // ASSERT
        assertTrue(historial.isEmpty());
    }

    private String dniUnico() {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        long ms = System.nanoTime() % 100000000L;
        return String.format("%08d", ms);
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
