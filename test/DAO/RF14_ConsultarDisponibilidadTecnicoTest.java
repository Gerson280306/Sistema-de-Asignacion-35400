package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF14_ConsultarDisponibilidadTecnicoTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Consulta de disponibilidad devuelve todos los tecnicos activos")
    void cp01_consultaDisponibilidad_devuelveTecnicosActivos() {
        // ACT
        List<Tecnico> activos = dao.listarActivos();

        // ASSERT
        assertNotNull(activos);
        assertFalse(activos.isEmpty(),
                "Debe haber tecnicos activos en la BD para consultar su disponibilidad");
        for (Tecnico t : activos) {
            assertEquals(1, t.getEstado(),
                    "listarActivos() solo debe devolver tecnicos con estado = 1");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP01b - La carga de trabajo de hoy se obtiene del mapa de ocupacion")
    void cp01b_mapaOcupacion_seObtieneParaFechaHoy() {
        // ACT
        Map<Integer, java.util.List<java.time.LocalTime[]>> ocupacion =
                dao.obtenerOcupacionPorFecha(LocalDate.now());

        // ASSERT
        assertNotNull(ocupacion,
                "El mapa de ocupacion nunca debe ser null, aunque este vacio");
        // Verificar que los ids de tecnico del mapa corresponden a tecnicos reales
        List<Tecnico> activos = dao.listarActivos();
        for (Integer idTec : ocupacion.keySet()) {
            boolean existe = activos.stream()
                    .anyMatch(t -> t.getIdTecnico() == idTec);
            assertTrue(existe,
                    "El mapa de ocupacion no debe contener ids de tecnicos inexistentes");
        }
    }

    @Test
    @Order(3)
    @DisplayName("CP01c - contarLibresHoy devuelve un numero no negativo de tecnicos libres")
    void cp01c_contarLibresHoy_devuelveNumeroNoNegativo() {
        // ACT
        int libres = dao.contarLibresHoy();

        // ASSERT
        assertTrue(libres >= 0,
                "El numero de tecnicos libres no puede ser negativo");
        assertTrue(libres <= dao.listarActivos().size(),
                "Los tecnicos libres no pueden superar al total de activos");
    }

    @Test
    @Order(4)
    @Disabled("Requiere BD de pruebas sin tecnicos activos. "
            + "Verificar manualmente desactivando todos los tecnicos y comprobando "
            + "que el sistema muestra un mensaje informativo en la pantalla de disponibilidad.")
    @DisplayName("CP02 - Sin tecnicos activos muestra vista vacia (requiere BD separada)")
    void cp02_sinTecnicosActivos_requiereBDSeparada() { }
}
