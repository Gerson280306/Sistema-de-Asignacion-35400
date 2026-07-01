package Runner;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Punto de entrada alternativo al JUnit5 ConsoleLauncher.
 *
 * En vez de imprimir el arbol de resultados "en vivo" (como hace
 * --details=tree), este runner ejecuta TODOS los tests primero y,
 * recien cuando terminan, imprime:
 *
 *   1) El resumen por RF, en orden (RF01, RF02, ... RF50).
 *   2) El detalle por CP, debajo.
 *
 * Asi el resumen siempre aparece al principio del reporte.
 */
public class RunAllTests {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("DAO"),
                        selectPackage("Controlador")
                )
                .build();

        Launcher launcher = LauncherFactory.create();
        ResumenRFListener resumen = new ResumenRFListener();
        launcher.registerTestExecutionListeners(resumen);

        launcher.execute(request);

        resumen.imprimirResumen();
        resumen.imprimirDetalle();
    }
}
