package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF10 — Editar tecnico
 * (En Caso_Prueba_KDXQLE.xlsx esta hoja no trae el nombre del requerimiento
 * en el encabezado, pero su contenido -"Edicion correcta" / "Campos
 * obligatorios vacios"- corresponde a editar un tecnico ya registrado.)
 */
public class RF10_EditarTecnicoTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @DisplayName("CP01 - Edicion correcta de los datos de un tecnico")
    void edicionCorrecta_seActualiza() {
        // ARRANGE
        String dni = dniUnico();
        dao.guardar(tecnicoValido(dni));
        Tecnico t = dao.buscar(dni).get(0);
        t.setTelefono("955566677");

        // ACT
        boolean actualizado = dao.actualizar(t);

        // ASSERT
        assertTrue(actualizado);
        Tecnico recargado = dao.buscar(dni).get(0);
        assertEquals("955566677", recargado.getTelefono());
    }

    @Test
    @DisplayName("CP02 - Campos obligatorios vacios impiden la actualizacion")
    void camposObligatoriosVacios_impideActualizacion() {
        // ARRANGE
        String dni = dniUnico();
        dao.guardar(tecnicoValido(dni));
        Tecnico t = dao.buscar(dni).get(0);
        t.setNombres(null); // nombres es NOT NULL en la base de datos

        // ACT
        boolean actualizado = dao.actualizar(t);

        // ASSERT
        assertFalse(actualizado, "No debe poder actualizar si un campo obligatorio queda vacio");
    }

    private Tecnico tecnicoValido(String dni) {
        Tecnico t = new Tecnico();
        t.setDni(dni);
        t.setNombres("TecnicoQA");
        t.setApellidos("PruebaAutomatizada");
        t.setTelefono("987654321");
        t.setEmail("tecnicoqa@gmail.com");
        t.setIdEspecialidad(primerIdEspecialidad());
        t.setIdZona(0);
        t.setMaxSolicitudesDia(6);
        t.setObservaciones("Generado por test automatizado");
        t.setEstado(1);
        return t;
    }

    private String dniUnico() {
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}
        long ms = System.nanoTime() % 100000000L;
        return String.format("%08d", ms);
    }

    private int primerIdEspecialidad() {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_especialidad FROM tb_especialidad LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
