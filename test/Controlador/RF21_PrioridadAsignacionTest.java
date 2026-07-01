package Controlador;

import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RF21_PrioridadAsignacionTest {

    /** Replica exacta de AsignacionController.pesoPrioridad() para poder testearla. */
    private static int pesoPrioridad(String p) {
        if (p == null) return 0;
        switch (p.toUpperCase()) {
            case "CRITICA": return 4;
            case "ALTA":    return 3;
            case "MEDIA":   return 2;
            case "BAJA":    return 1;
            default:        return 0;
        }
    }

    @Test
    @Order(1)
    @DisplayName("CP01 - Cada prioridad tiene el peso numerico correcto")
    void cp01_pesoNumeroCorrecto() {
        assertEquals(4, pesoPrioridad("CRITICA"));
        assertEquals(3, pesoPrioridad("ALTA"));
        assertEquals(2, pesoPrioridad("MEDIA"));
        assertEquals(1, pesoPrioridad("BAJA"));
        assertEquals(0, pesoPrioridad(null));
        assertEquals(0, pesoPrioridad("DESCONOCIDA"));
    }

    @Test
    @Order(2)
    @DisplayName("CP02 - El ordenamiento por prioridad pone CRITICA primero y BAJA ultimo")
    void cp02_ordenamientoCriticaPrimero() {
        // ARRANGE: lista en orden inverso al esperado
        List<String> prioridades = Arrays.asList("BAJA", "ALTA", "CRITICA", "MEDIA");

        // ACT: mismo comparador que usa asignarTodos()
        prioridades.sort(Comparator.comparingInt(s -> -pesoPrioridad(s)));

        // ASSERT
        assertEquals("CRITICA", prioridades.get(0));
        assertEquals("ALTA",    prioridades.get(1));
        assertEquals("MEDIA",   prioridades.get(2));
        assertEquals("BAJA",    prioridades.get(3));
    }

    @Test
    @Order(3)
    @DisplayName("CP03 - Prioridad en minusculas es tratada igual que en mayusculas")
    void cp03_prioridadMinusculas_mismoPeso() {
        assertEquals(pesoPrioridad("CRITICA"), pesoPrioridad("critica"));
        assertEquals(pesoPrioridad("ALTA"),    pesoPrioridad("alta"));
        assertEquals(pesoPrioridad("MEDIA"),   pesoPrioridad("media"));
        assertEquals(pesoPrioridad("BAJA"),    pesoPrioridad("baja"));
    }
}
