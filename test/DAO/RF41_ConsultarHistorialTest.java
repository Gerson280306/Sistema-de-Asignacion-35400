package DAO;

import Modelo.Solicitud;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF41_ConsultarHistorialTest {

    private final SolicitudDAO dao = new SolicitudDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Busqueda con texto existente devuelve al menos un registro del historial")
    void cp01_busquedaConTextoExistente_devuelveRegistros() {
        // ARRANGE: "Juan" es el nombre del cliente con id=1 en la BD semilla
        String textoBusqueda = "Juan";

        // ACT
        List<Solicitud> resultado = dao.buscar(textoBusqueda);

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(),
                "Debe existir al menos una solicitud del cliente 'Juan' en la BD semilla");
        assertTrue(resultado.stream()
                .anyMatch(s -> s.getNombreCliente() != null
                        && s.getNombreCliente().contains(textoBusqueda)),
                "Los resultados deben corresponder al texto buscado");
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Busqueda sin coincidencias devuelve lista vacia")
    void cp02_busquedaSinCoincidencias_listaVacia() {
        // ACT
        List<Solicitud> resultado = dao.buscar("ZZZ_CLIENTE_INEXISTENTE_999");

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty(),
                "Un texto sin coincidencias debe devolver lista vacia");
    }
}
