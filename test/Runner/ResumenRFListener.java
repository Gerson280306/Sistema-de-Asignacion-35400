package Runner;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Agrupa el resultado de cada Caso de Prueba (CP) por su RF
 * (Requerimiento Funcional, ej. "RF31") y arma:
 *
 *   1) Un resumen en orden (RF01, RF02, ... RF50): un RF se marca
 *      PASSED si ninguno de sus CP fallo. Los CP saltados/deshabilitados
 *      (@Disabled, Assumptions) tambien cuentan como PASSED, tal como
 *      se pidio.
 *
 *   2) Un detalle, en el mismo orden, con el resultado de cada CP.
 *
 * Ambos se imprimen DESPUES de que termina toda la ejecucion, por lo
 * que el resumen siempre queda arriba del detalle (nada se imprime
 * "en vivo" durante el run).
 */
public class ResumenRFListener implements TestExecutionListener {

    private static final Pattern PATRON_RF = Pattern.compile("^(RF\\d+)");

    private enum Estado { PASSED, FAILED }

    private static class Resultado {
        final String nombreCP;
        final Estado estado;
        Resultado(String nombreCP, Estado estado) {
            this.nombreCP = nombreCP;
            this.estado = estado;
        }
    }

    private final Map<String, List<Resultado>> porRF = new LinkedHashMap<>();
    private final Map<String, Integer> numeroRF = new HashMap<>();

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest()) return;
        Estado estado = testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED
                ? Estado.FAILED : Estado.PASSED;
        registrar(testIdentifier, estado);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (!testIdentifier.isTest()) return;
        registrar(testIdentifier, Estado.PASSED);
    }

    private void registrar(TestIdentifier testIdentifier, Estado estado) {
        String claseFuente = testIdentifier.getSource()
                .filter(s -> s instanceof MethodSource)
                .map(s -> ((MethodSource) s).getClassName())
                .orElse(null);
        if (claseFuente == null) return;

        String simple = claseFuente.substring(claseFuente.lastIndexOf('.') + 1);
        Matcher m = PATRON_RF.matcher(simple);
        if (!m.find()) return;
        String rf = m.group(1);

        porRF.computeIfAbsent(rf, k -> new ArrayList<>())
                .add(new Resultado(testIdentifier.getDisplayName(), estado));
        numeroRF.putIfAbsent(rf, Integer.parseInt(rf.substring(2)));
    }

    private List<String> rfEnOrden() {
        List<String> lista = new ArrayList<>(porRF.keySet());
        lista.sort(Comparator.comparingInt(numeroRF::get));
        return lista;
    }

    public void imprimirResumen() {
        System.out.println("================ RESUMEN POR RF ================");
        for (String rf : rfEnOrden()) {
            boolean falloAlgo = porRF.get(rf).stream().anyMatch(r -> r.estado == Estado.FAILED);
            System.out.println(rf + ": " + (falloAlgo ? "FAILED" : "PASSED"));
        }
        System.out.println("==================================================");
    }

    public void imprimirDetalle() {
        System.out.println();
        System.out.println("================ DETALLE POR CP =================");
        for (String rf : rfEnOrden()) {
            System.out.println(rf + ":");
            for (Resultado r : porRF.get(rf)) {
                System.out.println("   [" + r.estado + "] " + r.nombreCP);
            }
        }
        System.out.println("==================================================");
    }
}
