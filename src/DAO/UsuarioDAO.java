package DAO;

import Conexion.ConexionDB;
import Modelo.Usuario;
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

    /**
     * Autentica usuario comparando hash SHA-256 de la contraseña.
     * @return Usuario autenticado o null si falla.
     */
    public Usuario autenticar(String username, String password) {
        String sql = "SELECT * FROM tb_usuario WHERE username = ? AND estado = 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashGuardado = rs.getString("password_hash");
                String hashIngresado = sha256(password);
                if (hashGuardado.equals(hashIngresado)) {
                    // Actualizar ultimo_acceso
                    actualizarUltimoAcceso(rs.getInt("id_usuario"));
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO.autenticar] " + e.getMessage());
        }
        return null;
    }

    //--- Restablecer Contraseña (ABEL)
    public String restablecerPassword(String username) {

    String nuevaPassword =
            "Temp" + (int)(Math.random() * 9000 + 1000);

    try {

        String sql =
            "UPDATE tb_usuario "
          + "SET password_hash=? "
          + "WHERE username=?";
        
       

        Connection cn = ConexionDB.getInstancia().getConexion();

        PreparedStatement ps =
                cn.prepareStatement(sql);

        ps.setString(
            1,
            sha256(nuevaPassword)
        );

        ps.setString(2, username);

        int filas = ps.executeUpdate();

        if(filas > 0) {
            return nuevaPassword;
        }

    } catch(Exception e) {
        e.printStackTrace();
    }

    return null;
}
    
    
    // ─── CRUD ────────────────────────────────────────────────────────────────

    public List<Usuario> listarGestores() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_usuario WHERE rol IN ('SUPERVISOR','OPERADOR') ORDER BY fecha_creacion DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO.listarGestores] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.listarTodos] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.insertar] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.actualizar] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.cambiarPassword] " + e.getMessage());
            return false;
        }
    }

    public boolean toggleEstado(int idUsuario, int nuevoEstado) {
        String sql = "UPDATE tb_usuario SET estado=? WHERE id_usuario=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UsuarioDAO.toggleEstado] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.usernameExiste] " + e.getMessage());
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
            System.err.println("[UsuarioDAO.actualizarUltimoAcceso] " + e.getMessage());
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
