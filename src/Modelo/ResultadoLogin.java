package Modelo;

/**
 * Resultado detallado de un intento de inicio de sesion
 * (ver DAO.UsuarioDAO#autenticarDetallado).
 *
 * Permite que la pantalla de login distinga entre:
 *   - credenciales incorrectas (con cuantos intentos quedan antes del bloqueo)
 *   - cuenta bloqueada por exceso de intentos fallidos (requiere que un
 *     administrador la reactive manualmente)
 *
 * para poder avisarle al usuario, en vez de mostrar siempre el mismo
 * mensaje generico.
 */
public class ResultadoLogin {

    public enum Estado { OK, CREDENCIALES_INVALIDAS, CUENTA_BLOQUEADA }

    private final Estado estado;
    private final Usuario usuario;
    private final int intentosRestantes;

    private ResultadoLogin(Estado estado, Usuario usuario, int intentosRestantes) {
        this.estado = estado;
        this.usuario = usuario;
        this.intentosRestantes = intentosRestantes;
    }

    public static ResultadoLogin ok(Usuario usuario) {
        return new ResultadoLogin(Estado.OK, usuario, -1);
    }

    /** @param intentosRestantes cuantos intentos quedan antes del bloqueo (-1 si no aplica). */
    public static ResultadoLogin credencialesInvalidas(int intentosRestantes) {
        return new ResultadoLogin(Estado.CREDENCIALES_INVALIDAS, null, intentosRestantes);
    }

    public static ResultadoLogin bloqueada() {
        return new ResultadoLogin(Estado.CUENTA_BLOQUEADA, null, 0);
    }

    public Estado getEstado() { return estado; }
    public Usuario getUsuario() { return usuario; }
    public int getIntentosRestantes() { return intentosRestantes; }
}
