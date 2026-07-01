package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF19_ConsultarPorRangoFechasTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Consulta con rango valido devuelve solo solicitudes dentro de ese rango")
    void cp01_rangoValido_devuelveSolicitudesDentro() {
        // ARRANGE: rango que cubre todos los datos semilla
        LocalDate inicio = LocalDate.of(2020, 1, 1);
        LocalDate fin    = LocalDate.of(2030, 12, 31);

        // ACT
        List<Solicitud> resultado = dao.listarPorRango(inicio, fin);

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(),
                "Un rango amplio debe incluir las solicitudes de la BD semilla");
        // Solo validamos fechas no nulas (algunos registros semilla pueden tener fecha NULL)
        for (Solicitud s : resultado) {
            LocalDate fecha = s.getFechaSolicitada();
            if (fecha != null) {
                assertFalse(fecha.isBefore(inicio) || fecha.isAfter(fin),
                        "Las fechas devueltas deben estar dentro del rango consultado");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Rango invalido (fin antes que inicio) devuelve lista vacia sin excepcion")
    void cp02_rangoInvalido_devuelveListaVacia() {
        // ARRANGE
        LocalDate inicio = LocalDate.of(2030, 1, 1);
        LocalDate fin    = LocalDate.of(2020, 1, 1); // fin antes que inicio

        // ACT: validacion pura (sin BD)
        String errorValidacion = SolicitudDAO.validarRangoFechas(inicio, fin);
        List<Solicitud> resultadoDAO = dao.listarPorRango(inicio, fin);

        // ASSERT
        assertNotNull(errorValidacion,
                "La validacion debe detectar que fin < inicio");
        assertTrue(resultadoDAO.isEmpty(),
                "El DAO debe devolver lista vacia para un rango invalido, sin lanzar excepcion");
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Rango valido sin solicitudes en ese periodo devuelve lista vacia")
    void cp03_rangoValidoSinSolicitudes_listaVacia() {
        // ARRANGE: rango en el futuro lejano, sin datos
        LocalDate inicio = LocalDate.of(2090, 1, 1);
        LocalDate fin    = LocalDate.of(2090, 1, 31);

        // ACT
        List<Solicitud> resultado = dao.listarPorRango(inicio, fin);

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(),
                "Un rango sin solicitudes debe devolver lista vacia");
    }
}
