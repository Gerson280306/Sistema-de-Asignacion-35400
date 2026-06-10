package DAO;

import Conexion.ConexionDB;
import Modelo.Solicitud;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDAO {

    private Connection conn() {
        return ConexionDB.getInstancia().getConexion();
    }

    // ── LISTADOS ──────────────────────────────────────────────

    public List<Solicitud> listarTodos() {
        return listarConFiltro(null);
    }

    public List<Solicitud> listarPendientes() {
        return listarConFiltro("PENDIENTE");
    }

    /** Solicitudes cuyo estado aún puede cambiar automáticamente */
    public List<Solicitud> listarParaAutoTerminar() {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_cliente, s.id_tipo_servicio, "
                   + "s.descripcion, s.prioridad, "
                   + "s.fecha_registro, s.fecha_solicitada, s.horario_preferido, "
                   + "s.estado, s.observaciones, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_cliente, "
                   + "c.direccion AS direccion_cliente, "
                   + "ts.nombre AS nombre_tipo, "
                   + "CONCAT(IFNULL(t.nombres,''),' ',IFNULL(t.apellidos,'')) AS nombre_tecnico "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON c.id_cliente = s.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON ts.id_tipo_servicio = s.id_tipo_servicio "
                   + "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud "
                   + "LEFT JOIN tb_tecnico t ON t.id_tecnico = a.id_tecnico "
                   + "WHERE s.estado NOT IN ('COMPLETADA','CANCELADA') "
                   + "  AND s.fecha_solicitada <= CURDATE() "
                   + "ORDER BY s.id_solicitud DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] listarParaAutoTerminar: " + e.getMessage());
        }
        return lista;
    }

    /** Solicitudes PENDIENTES de HOY */
    public List<Solicitud> listarPendientesHoy() {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_cliente, s.id_tipo_servicio, "
                   + "s.descripcion, s.prioridad, "
                   + "s.fecha_registro, s.fecha_solicitada, s.horario_preferido, "
                   + "s.estado, s.observaciones, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_cliente, "
                   + "c.direccion AS direccion_cliente, "
                   + "ts.nombre AS nombre_tipo, "
                   + "CONCAT(IFNULL(t.nombres,''),' ',IFNULL(t.apellidos,'')) AS nombre_tecnico "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON c.id_cliente = s.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON ts.id_tipo_servicio = s.id_tipo_servicio "
                   + "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud "
                   + "LEFT JOIN tb_tecnico t ON t.id_tecnico = a.id_tecnico "
                   + "WHERE s.estado = 'PENDIENTE' "
                   + "  AND s.fecha_solicitada = CURDATE() "
                   + "ORDER BY s.id_solicitud DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] listarPendientesHoy: " + e.getMessage());
        }
        return lista;
    }

    private List<Solicitud> listarConFiltro(String estado) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_cliente, s.id_tipo_servicio, "
                   + "s.descripcion, s.prioridad, "
                   + "s.fecha_registro, s.fecha_solicitada, s.horario_preferido, "
                   + "s.estado, s.observaciones, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_cliente, "
                   + "c.direccion AS direccion_cliente, "
                   + "ts.nombre AS nombre_tipo, "
                   + "CONCAT(IFNULL(t.nombres,''),' ',IFNULL(t.apellidos,'')) AS nombre_tecnico "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON c.id_cliente = s.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON ts.id_tipo_servicio = s.id_tipo_servicio "
                   + "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud "
                   + "LEFT JOIN tb_tecnico t ON t.id_tecnico = a.id_tecnico "
                   + (estado != null ? "WHERE s.estado=? " : "")
                   + "ORDER BY s.id_solicitud DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (estado != null) ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] listarConFiltro: " + e.getMessage());
        }
        return lista;
    }

    public List<Solicitud> buscar(String texto) {
        List<Solicitud> lista = new ArrayList<>();
        String sql = "SELECT s.id_solicitud, s.id_cliente, s.id_tipo_servicio, "
                   + "s.descripcion, s.prioridad, "
                   + "s.fecha_registro, s.fecha_solicitada, s.horario_preferido, "
                   + "s.estado, s.observaciones, "
                   + "CONCAT(c.nombres,' ',c.apellidos) AS nombre_cliente, "
                   + "c.direccion AS direccion_cliente, "
                   + "ts.nombre AS nombre_tipo, "
                   + "CONCAT(IFNULL(t.nombres,''),' ',IFNULL(t.apellidos,'')) AS nombre_tecnico "
                   + "FROM tb_solicitud s "
                   + "JOIN tb_cliente c ON c.id_cliente = s.id_cliente "
                   + "JOIN tb_tipo_servicio ts ON ts.id_tipo_servicio = s.id_tipo_servicio "
                   + "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud "
                   + "LEFT JOIN tb_tecnico t ON t.id_tecnico = a.id_tecnico "
                   + "WHERE c.nombres LIKE ? OR c.apellidos LIKE ? "
                   + "ORDER BY s.id_solicitud DESC";
        String like = "%" + texto + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] buscar: " + e.getMessage());
        }
        return lista;
    }

    // ── ESCRITURA ─────────────────────────────────────────────

    public boolean guardar(Solicitud s) {
        String sql = "INSERT INTO tb_solicitud (id_cliente, id_tipo_servicio, "
                   + "descripcion, prioridad, "
                   + "fecha_solicitada, horario_preferido, estado, observaciones) "
                   + "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, s.getIdCliente());
            ps.setInt(2, s.getIdTipoServicio());
            ps.setString(3, s.getDescripcion());
            ps.setString(4, s.getPrioridad());
            if (s.getFechaSolicitada() != null)
                ps.setDate(5, java.sql.Date.valueOf(s.getFechaSolicitada()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, s.getHorarioPreferido());
            ps.setString(7, s.getEstado() != null ? s.getEstado() : "PENDIENTE");
            ps.setString(8, s.getObservaciones());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Solicitud s) {
        String sql = "UPDATE tb_solicitud SET id_cliente=?, id_tipo_servicio=?, "
                   + "descripcion=?, prioridad=?, "
                   + "fecha_solicitada=?, horario_preferido=?, estado=?, observaciones=? "
                   + "WHERE id_solicitud=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, s.getIdCliente());
            ps.setInt(2, s.getIdTipoServicio());
            ps.setString(3, s.getDescripcion());
            ps.setString(4, s.getPrioridad());
            if (s.getFechaSolicitada() != null)
                ps.setDate(5, java.sql.Date.valueOf(s.getFechaSolicitada()));
            else ps.setNull(5, Types.DATE);
            ps.setString(6, s.getHorarioPreferido());
            ps.setString(7, s.getEstado());
            ps.setString(8, s.getObservaciones());
            ps.setInt(9, s.getIdSolicitud());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] actualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstado(int id, String nuevoEstado) {
        String sql = "UPDATE tb_solicitud SET estado=? WHERE id_solicitud=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, nuevoEstado); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] cambiarEstado: " + e.getMessage());
            return false;
        }
    }

    // ── CONSULTAS AUXILIARES PARA EL ALGORITMO ────────────────

    public int obtenerEspecialidadDeTipo(int idTipoServicio) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id_especialidad FROM tb_tipo_servicio WHERE id_tipo_servicio=? LIMIT 1")) {
            ps.setInt(1, idTipoServicio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] obtenerEspecialidadDeTipo: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerZonaDeCliente(int idCliente) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id_zona FROM tb_cliente WHERE id_cliente=? LIMIT 1")) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                return rs.wasNull() ? 0 : z;
            }
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] obtenerZonaDeCliente: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Lista las horas (horario_preferido) de solicitudes PENDIENTES
     * en una fecha dada que aún no tienen técnico asignado.
     */
    public List<LocalTime> listarHorasPendientesPorFecha(LocalDate fecha) {
        List<LocalTime> lista = new ArrayList<>();
        String sql = "SELECT s.horario_preferido "
                   + "FROM tb_solicitud s "
                   + "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud "
                   + "  AND a.estado_asignacion NOT IN ('CANCELADA','COMPLETADA') "
                   + "WHERE s.fecha_solicitada = ? "
                   + "  AND s.estado = 'PENDIENTE' "
                   + "  AND a.id_asignacion IS NULL";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String h = rs.getString("horario_preferido");
                if (h == null) continue;
                try {
                    String hStr = h.length() > 5 ? h.substring(0, 5) : h;
                    lista.add(LocalTime.parse(hStr));
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            System.err.println("[SolicitudDAO] listarHorasPendientesPorFecha: " + e.getMessage());
        }
        return lista;
    }

    // ── PRIVADOS ──────────────────────────────────────────────

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdCliente(rs.getInt("id_cliente"));
        s.setNombreCliente(rs.getString("nombre_cliente"));
        s.setIdTipoServicio(rs.getInt("id_tipo_servicio"));
        s.setNombreTipo(rs.getString("nombre_tipo"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setPrioridad(rs.getString("prioridad"));
        s.setDireccionCliente(rs.getString("direccion_cliente"));
        s.setNombreTecnico(null); // se sobreescribe abajo si existe
        java.sql.Date fd = rs.getDate("fecha_solicitada");
        if (fd != null) s.setFechaSolicitada(fd.toLocalDate());
        s.setHorarioPreferido(rs.getString("horario_preferido"));
        s.setEstado(rs.getString("estado"));
        s.setObservaciones(rs.getString("observaciones"));
        try { s.setNombreTecnico(rs.getString("nombre_tecnico")); } catch (SQLException ignored) {}
        return s;
    }
}
