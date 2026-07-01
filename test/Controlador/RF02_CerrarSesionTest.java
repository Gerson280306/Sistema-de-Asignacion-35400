package Controlador;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RF02_CerrarSesionTest {

    @Test
    @Disabled("UI-only (Alert + cambio de Scene). Verificar manualmente: clic en "
            + "'Cerrar sesion' -> aparece dialogo de confirmacion -> clic en Si -> "
            + "vuelve a LoginView.")
    @DisplayName("CP01 - Cierre de sesion valido (evidencia manual)")
    void cierreValido_requiereEvidenciaManual() { /* sin logica de negocio que probar */ }

    @Test
    @Disabled("RF02-CP02 (token invalido / sesion expirada) no aplica a la arquitectura "
            + "actual: el sistema es una app de escritorio JavaFX con una unica conexion "
            + "JDBC (Conexion.ConexionDB), no maneja tokens de sesion como una app web. "
            + "Se recomienda ajustar este escenario en la ficha o aclarar con el profesor "
            + "si se espera implementar un mecanismo de sesion con expiracion.")
    @DisplayName("CP02 - Sesion expirada / token invalido (no aplica a esta arquitectura)")
    void sesionExpirada_noAplica() { }

    @Test
    @Disabled("UI-only (Alert). Verificar manualmente: clic en 'Cerrar sesion' -> "
            + "aparece dialogo -> clic en No -> la sesion continua activa, sigue en "
            + "la misma pantalla.")
    @DisplayName("CP03 - Gestor cancela la confirmacion (evidencia manual)")
    void cancelaConfirmacion_requiereEvidenciaManual() { }
}
