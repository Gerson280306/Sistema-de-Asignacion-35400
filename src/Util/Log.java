package Util;

/**
 * Utilidad simple de logging para el sistema.
 *
 * Antes, los DAO/Controladores usaban System.err.println(...) y
 * e.printStackTrace(), que escriben en el flujo de error estandar
 * (stderr). La consola de NetBeans (y la mayoria de IDEs) pinta ese
 * flujo en color rojo, asi que aunque la excepcion estuviera
 * correctamente controlada (y los tests pasaran igual), se veian
 * mensajes en rojo que parecian errores reales.
 *
 * Esta clase centraliza esos mensajes y los envia por System.out,
 * para que se vean en consola con un prefijo claro ([WARN]/[ERROR])
 * pero sin el color rojo de stderr.
 */
public final class Log {

    private Log() {}

    /** Para errores controlados (ej. fallo de SQL ya manejado). */
    public static void warn(String mensaje) {
        System.out.println("[WARN] " + mensaje);
    }

    /** Para excepciones con mensaje propio + causa. */
    public static void error(String mensaje, Throwable t) {
        System.out.println("[ERROR] " + mensaje + (t != null ? ": " + t.getMessage() : ""));
    }

    /** Equivalente a e.printStackTrace() pero sin escribir en stderr. */
    public static void error(Throwable t) {
        if (t == null) {
            System.out.println("[ERROR] excepcion desconocida");
            return;
        }
        System.out.println("[ERROR] " + t);
        for (StackTraceElement el : t.getStackTrace()) {
            System.out.println("    en " + el);
        }
    }
}
