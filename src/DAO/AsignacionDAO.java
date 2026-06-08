package DAO;

import Conexion.ConexionDB;
import Modelo.Asignacion;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO para la tabla tb_asignacion.
 */
public class AsignacionDAO {

    private Connection conn() {
        return ConexionDB.getInstancia().getConexion();
    }

    /**
     * Guarda una nueva asignación y actualiza el estado de la solicitud a ASIGNADA.
     */
    public boolean guardar(Asignacion a) {
        String sql = "INSERT INTO tb_asignacion "
                   + "(id_solicitud, id_tecnico, tipo_asignacion, fecha_programada, "
                   + " estado_asignacion, observaciones) "
                   + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, a.getIdSolicitud());
            ps.setInt(2, a.getIdTecnico());
            ps.setString(3, a.getTipoAsignacion() != null ? a.getTipoAsignacion() : "AUTOMATICA");
            if (a.getFechaProgramada() != null)
                ps.setTimestamp(4, Timestamp.valueOf(a.getFechaProgramada()));
            else ps.setNull(4, Types.TIMESTAMP);
            ps.setString(5, a.getEstadoAsignacion() != null ? a.getEstadoAsignacion() : "ASIGNADA");
            ps.setString(6, a.getObservaciones());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AsignacionDAO] guardar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia el estado de una asignación, opcionalmente reasigna técnico y graba observaciones.
     *
     * @param idAsignacion  ID de la asignación
     * @param nuevoEstado   Nuevo estado (ASIGNADA, EN_CAMINO, EN_PROCESO, COMPLETADA, CANCELADA)
     * @param idTecnicoNuevo  Nuevo técnico (-1 si no cambia)
     * @param observaciones   Notas de cierre
     */
    public boolean cambiarEstado(int idAsignacion, String nuevoEstado, int idTecnicoNuevo, String observaciones) {
        if (idAsignacion < 0) return false;
        StringBuilder sql = new StringBuilder(
            "UPDATE tb_asignacion SET estado_asignacion=?, observaciones=CONCAT(IFNULL(observaciones,''), ?) ");
        if (idTecnicoNuevo > 0) sql.append(", id_tecnico=").append(idTecnicoNuevo).append(" ");
        sql.append("WHERE id_asignacion=?");
        try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
            ps.setString(1, nuevoEstado);
            ps.setString(2, observaciones != null && !observaciones.isBlank()
                    ? "\n" + observaciones : "");
            ps.setInt(3, idAsignacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[AsignacionDAO] cambiarEstado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca el id_asignacion dado un id_solicitud.
     */
    public int buscarIdPorSolicitud(int idSolicitud) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id_asignacion FROM tb_asignacion WHERE id_solicitud=? LIMIT 1")) {
            ps.setInt(1, idSolicitud);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[AsignacionDAO] buscarIdPorSolicitud: " + e.getMessage());
        }
        return -1;
    }
}
