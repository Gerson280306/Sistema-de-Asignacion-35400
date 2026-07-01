package DAO;

import Modelo.Cliente;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RF08 — Listar cliente
 */
public class RF08_ListarClienteTest {

    private final ClienteDAO dao = new ClienteDAO();

    @Test
    @DisplayName("CP01 - El listado carga todos los clientes registrados")
    void cargaListadoCompleto() {
        // ACT
        List<Cliente> lista = dao.listarTodos();

        // ASSERT
        assertNotNull(lista);
        assertFalse(lista.isEmpty(), "Ya hay clientes registrados en la base, la lista no debe venir vacia");
        // El filtrado por texto/estado y la seleccion de fila ocurren en memoria sobre
        // esta misma lista, dentro de ClienteController.aplicarFiltros() (JavaFX puro).
    }

    @Test
    @Disabled("Probar 'sin clientes registrados' contra la base de datos real implicaria "
            + "vaciar tb_cliente, lo cual borraria datos reales del proyecto. Para esta "
            + "evidencia se recomienda usar una base de datos de pruebas vacia aparte, o "
            + "verificarlo manualmente creando una BD nueva sin datos.")
    @DisplayName("CP02 - Sin clientes registrados (requiere BD de pruebas separada)")
    void sinClientesRegistrados_requiereBDSeparada() { }

    @Test
    @Disabled("UI-only (ordenamiento de columnas de un TableView de JavaFX). Verificar "
            + "manualmente haciendo clic en los encabezados de la tabla.")
    @DisplayName("CP03 - Ordenar por columna (evidencia manual)")
    void ordenarPorColumna_requiereEvidenciaManual() { }
}
