package DAO;

import Controlador.SolicitudController;
import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF15_RegistrarSolicitudTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Registro de solicitud con datos validos se guarda en estado PENDIENTE")
    void cp01_registroValido_guardaEnEstadoPendiente() {
        // ARRANGE
        int idCliente = primerIdCliente();
        int idTipo    = primerIdTipoServicio();
        Solicitud s = new Solicitud();
        s.setIdCliente(idCliente);
        s.setIdTipoServicio(idTipo);
        s.setDescripcion("Solicitud QA automatizada RF15");
        s.setPrioridad("MEDIA");
        s.setEstado("PENDIENTE");
        s.setFechaSolicitada(LocalDate.now().plusDays(1));
        s.setHorarioPreferido("09:00");

        // ACT
        boolean guardado = dao.guardar(s);

        // ASSERT
        assertTrue(guardado, "La solicitud con datos validos debe guardarse");
        // Verificar que quedo en estado PENDIENTE
        Solicitud recargada = buscarUltimaDeCliente(idCliente);
        assertNotNull(recargada);
        assertEquals("PENDIENTE", recargada.getEstado());
    }

    @Test
    @Order(2)
    @Disabled("CP02 - Cliente no registrado: en la BD hay FK que lo impide automaticamente "
            + "(INSERT falla con error de integridad referencial). Ya cubierto por RF04-CP04. "
            + "En la UI se controla via ComboBox que solo muestra clientes registrados.")
    @DisplayName("CP02 - Cliente no registrado es impedido por la BD (cubierto por FK)")
    void cp02_clienteNoRegistrado_cubiertoporFK() { }

    @Test
    @Order(3)
    @DisplayName("CP03 - Hora fuera del rango 08:00-17:00 es rechazada antes de guardar")
    void cp03_horaFueraDeRango_esRechazada() {
        // ACT + ASSERT: antes de las 08:00
        assertNotNull(SolicitudController.validarHorario("07", "59"),
                "Las 07:59 deben ser rechazadas");
        // Despues de las 17:00
        assertNotNull(SolicitudController.validarHorario("17", "01"),
                "Las 17:01 deben ser rechazadas");
        // Exactamente en el limite inferior valido
        assertNull(SolicitudController.validarHorario("08", "00"),
                "Las 08:00 son validas");
        // Exactamente en el limite superior valido
        assertNull(SolicitudController.validarHorario("17", "00"),
                "Las 17:00 son validas");
    }

    @Test
    @Order(4)
    @DisplayName("CP04 - Tipo de servicio nulo no llega a la base de datos")
    void cp04_tipoServicioNulo_noSeGuarda() {
        // ARRANGE: solicitud sin tipo de servicio (id = 0 viola la FK)
        Solicitud s = new Solicitud();
        s.setIdCliente(primerIdCliente());
        s.setIdTipoServicio(0); // 0 no existe en tb_tipo_servicio
        s.setDescripcion("Sin tipo");
        s.setPrioridad("BAJA");
        s.setEstado("PENDIENTE");
        s.setFechaSolicitada(LocalDate.now().plusDays(1));

        // ACT
        boolean guardado = dao.guardar(s);

        // ASSERT
        assertFalse(guardado,
                "Una solicitud sin tipo de servicio valido no debe guardarse");
    }

    private int primerIdCliente() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_cliente FROM tb_cliente LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int primerIdTipoServicio() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tipo_servicio FROM tb_tipo_servicio LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Solicitud buscarUltimaDeCliente(int idCliente) {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT * FROM tb_solicitud WHERE id_cliente=? "
                        + "ORDER BY id_solicitud DESC LIMIT 1")) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Solicitud s = new Solicitud();
            s.setIdSolicitud(rs.getInt("id_solicitud"));
            s.setEstado(rs.getString("estado"));
            return s;
        } catch (Exception e) { return null; }
    }
}
