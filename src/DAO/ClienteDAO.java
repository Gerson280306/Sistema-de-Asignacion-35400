package DAO;

import Util.Log;

import Conexion.ConexionDB;
import Modelo.Cliente;
import Modelo.Zona;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private Connection conn() {
        return ConexionDB.getInstancia().getConexion();
    }

    // ── LISTAR TODOS ─────────────────────────────────────────
    public List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.*, z.nombre AS nombre_zona FROM tb_cliente c "
                   + "LEFT JOIN tb_zona z ON z.id_zona = c.id_zona ORDER BY c.id_cliente DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] listarTodos: " + e.getMessage());
        }
        return lista;
    }

    // ── BUSCAR POR TEXTO ─────────────────────────────────────
    public List<Cliente> buscar(String texto) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT c.*, z.nombre AS nombre_zona FROM tb_cliente c "
                   + "LEFT JOIN tb_zona z ON z.id_zona = c.id_zona "
                   + "WHERE c.nombres LIKE ? OR c.apellidos LIKE ? OR c.dni LIKE ? "
                   + "OR c.telefono LIKE ? ORDER BY c.id_cliente DESC";
        String like = "%" + texto + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            ps.setString(3, like); ps.setString(4, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] buscar: " + e.getMessage());
        }
        return lista;
    }

    // ── GUARDAR (INSERT) ─────────────────────────────────────
    public boolean guardar(Cliente c) {
        String sql = "INSERT INTO tb_cliente (dni, nombres, apellidos, telefono, email, "
                   + "direccion, referencia, distrito, id_zona, observaciones, estado) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getDireccion());
            ps.setString(7, c.getReferencia());
            ps.setString(8, c.getDistrito());
            if (c.getIdZona() > 0) ps.setInt(9, c.getIdZona());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, c.getObservaciones());
            ps.setInt(11, c.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] guardar: " + e.getMessage());
            return false;
        }
    }

    // ── ACTUALIZAR (UPDATE) ──────────────────────────────────
    public boolean actualizar(Cliente c) {
        String sql = "UPDATE tb_cliente SET dni=?, nombres=?, apellidos=?, telefono=?, "
                   + "email=?, direccion=?, referencia=?, distrito=?, id_zona=?, "
                   + "observaciones=?, estado=? WHERE id_cliente=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getDireccion());
            ps.setString(7, c.getReferencia());
            ps.setString(8, c.getDistrito());
            if (c.getIdZona() > 0) ps.setInt(9, c.getIdZona());
            else ps.setNull(9, Types.INTEGER);
            ps.setString(10, c.getObservaciones());
            ps.setInt(11, c.getEstado());
            ps.setInt(12, c.getIdCliente());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] actualizar: " + e.getMessage());
            return false;
        }
    }

    // ── ELIMINAR (estado=0) ──────────────────────────────────
    public boolean eliminar(int id) {
        String sql = "UPDATE tb_cliente SET estado=0 WHERE id_cliente=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] eliminar: " + e.getMessage());
            return false;
        }
    }

    // ── VERIFICAR DNI DUPLICADO ──────────────────────────────
    public boolean existeDni(String dni, int idExcluir) {
        String sql = "SELECT id_cliente FROM tb_cliente WHERE dni=? AND id_cliente<>?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, dni); ps.setInt(2, idExcluir);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // ── SOLICITUDES ACTIVAS (RF05-CP02) ──────────────────────
    /**
     * Cuenta las solicitudes del cliente que no están en estado final
     * (no CANCELADA ni COMPLETADA). Se usa para bloquear la eliminación
     * de un cliente con servicios en curso.
     */
    public int contarSolicitudesActivas(int idCliente) {
        String sql = "SELECT COUNT(*) FROM tb_solicitud WHERE id_cliente=? "
                   + "AND estado NOT IN ('CANCELADA','COMPLETADA')";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            Log.warn("[ClienteDAO] contarSolicitudesActivas: " + e.getMessage());
        }
        return 0;
    }

    public boolean tieneSolicitudesActivas(int idCliente) {
        return contarSolicitudesActivas(idCliente) > 0;
    }

    // ── MAPEAR ResultSet → Cliente ───────────────────────────
    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setDni(rs.getString("dni"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        c.setReferencia(rs.getString("referencia"));
        c.setDistrito(rs.getString("distrito"));
        c.setIdZona(rs.getInt("id_zona"));
        c.setNombreZona(rs.getString("nombre_zona"));
        c.setObservaciones(rs.getString("observaciones"));
        c.setEstado(rs.getInt("estado"));
        return c;
    }
}
