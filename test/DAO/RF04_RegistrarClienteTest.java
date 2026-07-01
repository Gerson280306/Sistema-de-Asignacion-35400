package DAO;

import Controlador.ClienteController;
import Modelo.Cliente;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class RF04_RegistrarClienteTest {

    private final ClienteDAO dao = new ClienteDAO();

    @Test
    @DisplayName("CP01 - Registro de cliente con datos validos se guarda correctamente")
    void registroValido_seGuarda() {
        // ARRANGE
        String dniNuevo = dniUnico();
        Cliente c = clienteValido(dniNuevo);

        // ACT
        boolean guardado = dao.guardar(c);

        // ASSERT
        assertTrue(guardado);
        assertTrue(dao.existeDni(dniNuevo, 0), "El cliente debe quedar registrado con ese DNI");
    }

    @Test
    @DisplayName("CP02 - DNI duplicado es detectado antes de insertar")
    void dniDuplicado_esDetectado() {
        // ARRANGE: registramos un cliente y luego intentamos detectar su DNI como duplicado
        String dni = dniUnico();
        dao.guardar(clienteValido(dni));

        // ACT
        boolean existe = dao.existeDni(dni, 0);

        // ASSERT
        assertTrue(existe, "Un DNI ya registrado debe detectarse como duplicado");
    }

    @Test
    @DisplayName("CP03 - Campos vacios o con formato invalido no llegan a la base de datos")
    void camposInvalidos_sonRechazadosAntesDeLaBD() {
        // ACT + ASSERT: todo vacio
        assertNotNull(ClienteController.validarFormato("", "", "", "", "", ""));

        // DNI con letras
        assertNotNull(ClienteController.validarFormato("abc123", "Juan", "Perez", "", "", ""));

        // Nombres con numeros
        assertNotNull(ClienteController.validarFormato("12345678", "Juan123", "Perez", "", "", ""));

        // Telefono con menos de 9 digitos
        assertNotNull(ClienteController.validarFormato("12345678", "Juan", "Perez", "12345", "", ""));

        // Correo que no termina en @gmail.com
        assertNotNull(ClienteController.validarFormato("12345678", "Juan", "Perez", "", "", "juan@hotmail.com"));

        // Caso valido control: no debe haber error
        assertNull(ClienteController.validarFormato("12345678", "Juan", "Perez", "987654321", "Av. Test 123", "juan@gmail.com"));
    }

    @Test
    @DisplayName("CP04 - Error al insertar (violacion de restriccion de la BD) se maneja sin caerse")
    void errorSqlAlInsertar_devuelveFalse() {
        // ARRANGE: dni=null viola la restriccion NOT NULL de la columna
        Cliente c = clienteValido(dniUnico());
        c.setDni(null);

        // ACT
        boolean resultado = dao.guardar(c);

        // ASSERT
        assertFalse(resultado, "Un error SQL debe devolver false, no lanzar una excepcion al usuario");
    }

    private Cliente clienteValido(String dni) {
        Cliente c = new Cliente();
        c.setDni(dni);
        c.setNombres("ClienteQA");
        c.setApellidos("PruebaAutomatizada");
        c.setTelefono("987654321");
        c.setEmail("clienteqa@gmail.com");
        c.setDireccion("Av. de Pruebas 123");
        c.setReferencia("Cerca al parque");
        c.setIdZona(0);
        c.setEstado(1);
        return c;
    }

    private static final java.util.concurrent.atomic.AtomicLong CONTADOR_DNI = new java.util.concurrent.atomic.AtomicLong();

    /** Genera un DNI de 8 digitos unico para no chocar con datos existentes ni con corridas previas. */
    private String dniUnico() {
        long valor = (System.nanoTime() + CONTADOR_DNI.incrementAndGet()) % 100000000L;
        return String.format("%08d", Math.abs(valor));
    }
}
