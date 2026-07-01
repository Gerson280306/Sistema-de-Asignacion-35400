package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF36 - Gestionar horario de tecnico
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF36_GestionarHorarioTecnicoTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Guardar horario de lunes a viernes se registra correctamente")
    void cp01_guardarHorarioLunesViernes_seRegistra() {
        int idTecnico = crearTecnicoFixture();
        boolean[] dias = {true, true, true, true, true, false, false};

        boolean guardado = dao.guardarHorario(idTecnico, dias, "08:00", "17:00");

        assertTrue(guardado);
        boolean[] cargado = dao.cargarDiasHorario(idTecnico);
        for (int i = 0; i < 5; i++) {
            assertTrue(cargado[i], "El dia " + (i + 1) + " debe estar activo");
        }
        assertFalse(cargado[5], "Sabado no debe estar activo");
        assertFalse(cargado[6], "Domingo no debe estar activo");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Guardar horario reemplaza el anterior (no acumula)")
    void cp02_guardarHorarioNuevo_reemplazaAnterior() {
        int idTecnico = crearTecnicoFixture();
        boolean[] original = {true, true, true, true, true, false, false};
        dao.guardarHorario(idTecnico, original, "08:00", "17:00");

        boolean[] nuevo = {true, false, false, false, false, false, false};
        dao.guardarHorario(idTecnico, nuevo, "09:00", "18:00");

        boolean[] cargado = dao.cargarDiasHorario(idTecnico);
        assertTrue(cargado[0],  "Lunes debe estar activo");
        assertFalse(cargado[1], "Martes debe estar inactivo despues del reemplazo");
        assertFalse(cargado[2], "Miercoles debe estar inactivo despues del reemplazo");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Tecnico sin horario configurado devuelve todos los dias activos (comportamiento por defecto)")
    void cp03_tecnicoSinHorario_defaultTodosLosDias() {
        // ARRANGE: tecnico nuevo sin horario guardado
        int idTecnico = crearTecnicoFixture();

        // ACT
        boolean[] dias = dao.cargarDiasHorario(idTecnico);

        // ASSERT: el metodo usa Arrays.fill(true) como fallback cuando no hay horario,
        // para no bloquear la asignacion a tecnicos sin configuracion explicita
        assertNotNull(dias);
        assertEquals(7, dias.length);
        for (boolean d : dias) {
            assertTrue(d,
                    "Sin horario configurado, cargarDiasHorario asume disponibilidad total (todos los dias en true)");
        }
    }

    private int crearTecnicoFixture() {
        String dni = String.format("%08d", Math.abs(System.nanoTime() % 100000000L));
        Tecnico t = new Tecnico();
        t.setDni(dni);
        t.setNombres("TecnicoHorario");
        t.setApellidos("RF36");
        t.setTelefono("987000001");
        t.setEmail("techorario36@gmail.com");
        t.setIdEspecialidad(primerIdEspecialidad());
        t.setIdZona(1);
        t.setMaxSolicitudesDia(6);
        t.setEstado(1);
        dao.guardar(t);
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tecnico FROM tb_tecnico WHERE dni=?")) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private int primerIdEspecialidad() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_especialidad FROM tb_especialidad LIMIT 1")) {
            ResultSet rs = ps.executeQuery(); rs.next(); return rs.getInt(1);
        } catch (Exception e) { return 1; }
    }
}
