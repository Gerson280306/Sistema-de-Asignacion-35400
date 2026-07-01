package DAO;

import Modelo.Tecnico;
import org.junit.jupiter.api.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF12_BuscarTecnicoEspecialidadTest {

    private final TecnicoDAO dao = new TecnicoDAO();

    @Test
    @Order(1)
    @DisplayName("CP01 - Busqueda por especialidad existente devuelve solo los tecnicos de esa especialidad")
    void cp01_busquedaEspecialidadExistente_devuelveSoloEsa() throws Exception {
        // ARRANGE: obtenemos la primera especialidad que tenga al menos un tecnico
        int idEsp = primerIdEspecialidadConTecnico();

        // ACT
        List<Tecnico> resultado = dao.filtrar(idEsp, 0);

        // ASSERT
        assertFalse(resultado.isEmpty(),
                "Debe haber al menos un tecnico con la especialidad seleccionada");
        for (Tecnico t : resultado) {
            assertEquals(idEsp, t.getIdEspecialidad(),
                    "Todos los tecnicos del resultado deben tener la especialidad buscada");
        }
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - Busqueda por especialidad sin coincidencias devuelve lista vacia")
    void cp02_busquedaSinCoincidencias_listaVacia() {
        // ARRANGE: id de especialidad que no existe en la BD
        int idEspInexistente = 999999;

        // ACT
        List<Tecnico> resultado = dao.filtrar(idEspInexistente, 0);

        // ASSERT
        assertTrue(resultado.isEmpty(),
                "Una especialidad inexistente no debe devolver tecnicos");
    }

    private int primerIdEspecialidadConTecnico() throws Exception {
        try (PreparedStatement ps = Conexion.ConexionDB.getInstancia().getConexion()
                .prepareStatement(
                    "SELECT id_especialidad FROM tb_tecnico WHERE estado=1 LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Debe existir al menos un tecnico activo en la BD");
            return rs.getInt(1);
        }
    }
}
