package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF11_ListarTecnicoTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - El listado carga todos los tecnicos registrados")
    void cp01_listadoCargaTodosLosTecnicos() {
        // ACT
        List<Tecnico> lista = dao.listarTodos();

        // ASSERT
        assertNotNull(lista);
        assertFalse(lista.isEmpty(),
                "Ya hay tecnicos en la BD semilla, la lista no debe venir vacia");
    }

    @Test
    @Order(2)
    @Disabled("Requiere BD de pruebas separada sin tecnicos para no borrar datos reales. "
            + "Verificar manualmente creando una BD nueva vacia.")
    @DisplayName("CP02 - Sin tecnicos registrados muestra lista vacia (requiere BD separada)")
    void cp02_sinTecnicosListaVacia_requiereBDSeparada() { }
}
