package DAO;

import Util.Log;

import Conexion.ConexionDB;
import Modelo.Usuario;
import Modelo.ResultadoLogin;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UsuarioDAO.java
 * Ubicación: src/DAO/UsuarioDAO.java
 * CRUD de tb_usuario + autenticación con hash SHA-256.
 */
public class UsuarioDAO {

    private Connection getConn() {
        return ConexionDB.getInstancia().getConexion();
    }

    // ─── Autenticación ────────────────────────────────────────────────────────

    /** Intentos fallidos consecutivos permitidos antes de bloquear la cuenta (RF01-CP04). */
    public static final int MAX_INTENTOS_FALLIDOS = 3;

    /**
     * Autentica usuario comparando hash SHA-256 de la contraseña (atajo sobre
     * autenticarDetallado(), se mantiene para no romper el resto del código/tests).
     * @return Usuario autenticado o null si falla (credenciales incorrectas o cuenta bloqueada).
     */
    public Usuario autenticar(String username, String password) {
        return autenticarDetallado(username, password).getUsuario();
    }

    /**
     * Igual que autenticar(), pero devuelve el detalle del resultado: si las
     * credenciales son incorrectas (con cuántos intentos quedan) o si la cuenta
     * quedó bloqueada por exceso de intentos fallidos (requiere que un
     * administrador la reactive manualmente). Pensado para que la pantalla de
     * login arme un mensaje claro.
     *
     * Reglas:
     *  - Contraseña incorrecta: suma un intento fallido. Al llegar a
     *    MAX_INTENTOS_FALLIDOS, bloquea la cuenta (estado=0).
     *  - Una cuenta con estado=0 no puede autenticarse hasta que un admin la
     *    reactive (no hay desbloqueo automático).
     *  - Un login exitoso reinicia el contador de intentos a 0.
     */
    public ResultadoLogin autenticarDetallado(String username, String password) {
        String sql = "SELECT * FROM tb_usuario WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return ResultadoLogin.credencialesInvalidas(-1);
            }

            int idUsuario = rs.getInt("id_usuario");
            int estado = rs.getInt("estado");
            int intentosActuales = rs.getInt("intentos_fallidos");

            if (estado == 0) {
                // Bloqueada (por intentos fallidos o desactivada por un admin):
                // en ambos casos requiere que un administrador la reactive.
                return ResultadoLogin.bloqueada();
            }

            String hashGuardado = rs.getString("password_hash");
            String hashIngresado = sha256(password);
            if (hashGuardado.equals(hashIngresado)) {
                reiniciarIntentosFallidos(idUsuario);
                actualizarUltimoAcceso(idUsuario);
                Usuario u = mapear(rs);
                u.setEstado(estado);
                return ResultadoLogin.ok(u);
            }

            int restantes = registrarIntentoFallido(idUsuario, intentosActuales);
            if (restantes <= 0) {
                return ResultadoLogin.bloqueada();
            }
            return ResultadoLogin.credencialesInvalidas(restantes);
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.autenticarDetallado] " + e.getMessage());
            return ResultadoLogin.credencialesInvalidas(-1);
        }
    }

    /**
     * Suma un intento fallido; si llega al máximo, bloquea la cuenta (estado=0).
     * @return cuántos intentos quedan antes del bloqueo (0 si quedó bloqueada).
     */
    private int registrarIntentoFallido(int idUsuario, int intentosActuales) {
        int nuevosIntentos = intentosActuales + 1;
        boolean bloquear = nuevosIntentos >= MAX_INTENTOS_FALLIDOS;
        String sql = bloquear
            ? "UPDATE tb_usuario SET intentos_fallidos=?, estado=0 WHERE id_usuario=?"
            : "UPDATE tb_usuario SET intentos_fallidos=? WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, nuevosIntentos);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.registrarIntentoFallido] " + e.getMessage());
        }
        return Math.max(0, MAX_INTENTOS_FALLIDOS - nuevosIntentos);
    }

    private void reiniciarIntentosFallidos(int idUsuario) {
        String sql = "UPDATE tb_usuario SET intentos_fallidos=0 WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.reiniciarIntentosFallidos] " + e.getMessage());
        }
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<Usuario> listarGestores() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_usuario WHERE rol IN ('SUPERVISOR','OPERADOR') ORDER BY fecha_creacion DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.listarGestores] " + e.getMessage());
        }
        return lista;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_usuario ORDER BY fecha_creacion DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.listarTodos] " + e.getMessage());
        }
        return lista;
    }

    public boolean insertar(Usuario u, String passwordPlano) {
        String sql = "INSERT INTO tb_usuario (username, password_hash, nombre_completo, email, rol, estado) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, sha256(passwordPlano));
            ps.setString(3, u.getNombreCompleto());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getRol());
            ps.setInt(6, u.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.insertar] " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Usuario u) {
        String sql = "UPDATE tb_usuario SET username=?, nombre_completo=?, email=?, rol=?, estado=? WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getNombreCompleto());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getRol());
            ps.setInt(5, u.getEstado());
            ps.setInt(6, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.actualizar] " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarPassword(int idUsuario, String nuevaPasswordPlana) {
        String sql = "UPDATE tb_usuario SET password_hash=? WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, sha256(nuevaPasswordPlana));
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.cambiarPassword] " + e.getMessage());
            return false;
        }
    }

    public boolean toggleEstado(int idUsuario, int nuevoEstado) {
        String sql = (nuevoEstado == 1)
            ? "UPDATE tb_usuario SET estado=?, intentos_fallidos=0 WHERE id_usuario=?"
            : "UPDATE tb_usuario SET estado=? WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.toggleEstado] " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExiste(String username, int excludeId) {
        String sql = "SELECT COUNT(*) FROM tb_usuario WHERE username=? AND id_usuario<>?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, excludeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.usernameExiste] " + e.getMessage());
        }
        return false;
    }

    // ─── Auxiliares ──────────────────────────────────────────────────────────

    private void actualizarUltimoAcceso(int id) {
        String sql = "UPDATE tb_usuario SET ultimo_acceso=NOW() WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.actualizarUltimoAcceso] " + e.getMessage());
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setEmail(rs.getString("email"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getInt("estado"));
        Timestamp ua = rs.getTimestamp("ultimo_acceso");
        if (ua != null) u.setUltimoAcceso(ua.toLocalDateTime());
        Timestamp fc = rs.getTimestamp("fecha_creacion");
        if (fc != null) u.setFechaCreacion(fc.toLocalDateTime());
        return u;
    }

    // ─── Recuperación de contraseña (RF03) ─────────────────────────────────────
    // Nota: el envío real del correo NO se implementa aquí a propósito —
    // es responsabilidad de un servicio de correo aparte (SMTP), para que esta
    // clase se pueda probar con JUnit sin depender de una conexión de email real.

    private static final int MINUTOS_VIGENCIA_TOKEN = 30;

    /**
     * Genera un token de recuperación de un solo uso para el correo indicado.
     * @return el token generado, o null si no existe un usuario con ese correo.
     */
    public String generarTokenRecuperacion(String email) {
        Integer idUsuario = buscarIdPorEmail(email);
        if (idUsuario == null) return null;

        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        String sql = "INSERT INTO tb_recuperacion_password (id_usuario, token, fecha_expiracion) "
                   + "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, token);
            ps.setInt(3, MINUTOS_VIGENCIA_TOKEN);
            ps.executeUpdate();
            return token;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.generarTokenRecuperacion] " + e.getMessage());
            return null;
        }
    }

    /** @return true si el token existe, no fue usado y no ha expirado. */
    public boolean validarToken(String token) {
        // La comparación de fecha se hace en SQL (fecha_expiracion > NOW()) y no en Java
        // a propósito: la conexión usa serverTimezone=America/Lima pero el servidor MySQL
        // puede estar en otra zona horaria (SYSTEM); comparar Timestamps ya en Java quedó
        // desfasado por esa diferencia. Dejar que MySQL compare sus propias fechas evita el problema.
        String sql = "SELECT usado, (fecha_expiracion > NOW()) AS vigente "
                   + "FROM tb_recuperacion_password WHERE token=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            if (rs.getBoolean("usado")) return false;
            return rs.getBoolean("vigente");
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.validarToken] " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida la política mínima de contraseñas (RF03-CP05).
     * @return null si cumple la política, o un mensaje de error específico si no.
     */
    public static String validarPoliticaPassword(String password) {
        if (password == null || password.length() < 8)
            return "La contraseña debe tener al menos 8 caracteres.";
        if (!password.matches(".*\\d.*"))
            return "La contraseña debe incluir al menos un número.";
        return null;
    }

    /**
     * Actualiza la contraseña usando un token de recuperación válido y marca el token como usado.
     * @return true si se actualizó correctamente.
     */
    public boolean actualizarPasswordConToken(String token, String nuevaPasswordPlana) {
        if (validarPoliticaPassword(nuevaPasswordPlana) != null) return false;
        if (!validarToken(token)) return false;

        String sqlBuscar = "SELECT id_usuario FROM tb_recuperacion_password WHERE token=?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlBuscar)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;
            int idUsuario = rs.getInt("id_usuario");

            if (!cambiarPassword(idUsuario, nuevaPasswordPlana)) return false;

            try (PreparedStatement upd = getConn().prepareStatement(
                    "UPDATE tb_recuperacion_password SET usado=1 WHERE token=?")) {
                upd.setString(1, token);
                upd.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.actualizarPasswordConToken] " + e.getMessage());
            return false;
        }
    }

    private Integer buscarIdPorEmail(String email) {
        String sql = "SELECT id_usuario FROM tb_usuario WHERE email=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id_usuario");
        } catch (SQLException e) {
            Log.warn("[UsuarioDAO.buscarIdPorEmail] " + e.getMessage());
        }
        return null;
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear", e);
        }
    }
}
