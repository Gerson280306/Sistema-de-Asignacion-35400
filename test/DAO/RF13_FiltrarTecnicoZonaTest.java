package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF13_FiltrarTecnicoZonaTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Filtrado por zona con tecnicos asignados devuelve solo esa zona")
    void cp01_filtradoZonaConTecnicos_devuelveSoloEsa() throws Exception {
        // ARRANGE: zona que tenga al menos un tecnico
        int idZona = primerIdZonaConTecnico();

        // ACT
        List<Tecnico> resultado = dao.filtrar(0, idZona);

        // ASSERT
        assertFalse(resultado.isEmpty(),
                "Debe existir al menos un tecnico en la zona seleccionada");
        for (Tecnico t : resultado) {
            assertEquals(idZona, t.getIdZona(),
                    "Todos los tecnicos del resultado deben pertenecer a la zona filtrada");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Filtrado por zona sin tecnicos devuelve lista vacia")
    void cp02_filtradoZonaSinTecnicos_listaVacia() {
        // ARRANGE: id de zona que no existe
        int idZonaInexistente = 999999;

        // ACT
        List<Tecnico> resultado = dao.filtrar(0, idZonaInexistente);

        // ASSERT
        assertTrue(resultado.isEmpty(),
                "Una zona sin tecnicos no debe devolver resultados");
    }

    private int primerIdZonaConTecnico() throws Exception {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement(
                    "SELECT id_zona FROM tb_tecnico WHERE estado=1 AND id_zona > 0 LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Debe existir al menos un tecnico activo con zona asignada");
            return rs.getInt(1);
        }
    }
}
