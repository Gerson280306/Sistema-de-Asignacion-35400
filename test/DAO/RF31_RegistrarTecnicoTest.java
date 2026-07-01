package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF31_RegistrarTecnicoTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Registro de tecnico con datos validos se guarda correctamente")
    void cp01_registroValido_seGuarda() {
        String dni = dniUnico();
        Tecnico t = tecnicoValido(dni);

        boolean guardado = dao.guardar(t);

        assertTrue(guardado);
        assertTrue(dao.existeDni(dni, 0), "El tecnico debe quedar registrado con ese DNI");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - DNI duplicado es detectado antes de insertar")
    void cp02_dniDuplicado_esDetectado() {
        String dni = dniUnico();
        dao.guardar(tecnicoValido(dni));

        boolean existe = dao.existeDni(dni, 0);

        assertTrue(existe, "Un DNI ya registrado debe detectarse como duplicado");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Registro con nombre nulo es rechazado por la BD sin excepcion")
    void cp03_nombreNulo_devuelveFalse() {
        Tecnico t = tecnicoValido(dniUnico());
        t.setNombres(null);

        boolean guardado = dao.guardar(t);

        assertFalse(guardado, "Un tecnico con nombre nulo no debe guardarse");
    }

    private Tecnico tecnicoValido(String dni) {
        Tecnico t = new Tecnico();
        t.setDni(dni);
        t.setNombres("TecnicoQA");
        t.setApellidos("RF31");
        t.setTelefono("987654321");
        t.setEmail("tecnicoqa31@gmail.com");
        t.setIdEspecialidad(primerIdEspecialidad());
        t.setIdZona(1);
        t.setMaxSolicitudesDia(5);
        t.setObservaciones("Creado por test RF31");
        t.setEstado(1);
        return t;
    }

    private String dniUnico() {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        return String.format("%08d", Math.abs(System.nanoTime() % 100000000L));
    }

    private int primerIdEspecialidad() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_especialidad FROM tb_especialidad LIMIT 1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { return 1; }
    }
}
