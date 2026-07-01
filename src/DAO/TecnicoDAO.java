package DAO;

import Util.Log;

import Conexion.ConexionDB;
import Modelo.Tecnico;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class TecnicoDAO {

    private Connection conn() {
        return ConexionDB.getInstancia().getConexion();
    }

    // ── LISTADOS ──────────────────────────────────────────────

    public List<Tecnico> listarTodos() {
        return listarConFiltro(false);
    }

    /**
     * Lista solo técnicos activos (estado=1).
     * El algoritmo de asignación verifica disponibilidad horaria por sí mismo.
     */
    public List<Tecnico> listarActivos() {
        return listarConFiltro(true);
    }

    private List<Tecnico> listarConFiltro(boolean soloActivos) {
        List<Tecnico> lista = new ArrayList<>();
        String sql = "SELECT t.*, e.nombre AS nombre_especialidad, z.nombre AS nombre_zona "
                   + "FROM tb_tecnico t "
                   + "LEFT JOIN tb_especialidad e ON e.id_especialidad = t.id_especialidad "
                   + "LEFT JOIN tb_zona z ON z.id_zona = t.id_zona "
                   + (soloActivos ? "WHERE t.estado=1 " : "")
                   + "ORDER BY t.id_tecnico ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] listarConFiltro: " + e.getMessage());
        }
        return lista;
    }

    public List<Tecnico> buscar(String texto) {
        List<Tecnico> lista = new ArrayList<>();
        String sql = "SELECT t.*, e.nombre AS nombre_especialidad, z.nombre AS nombre_zona "
                   + "FROM tb_tecnico t "
                   + "LEFT JOIN tb_especialidad e ON e.id_especialidad = t.id_especialidad "
                   + "LEFT JOIN tb_zona z ON z.id_zona = t.id_zona "
                   + "WHERE t.nombres LIKE ? OR t.apellidos LIKE ? OR t.dni LIKE ? "
                   + "ORDER BY t.id_tecnico DESC";
        String like = "%" + texto + "%";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] buscar: " + e.getMessage());
        }
        return lista;
    }

    public List<Tecnico> filtrar(int idEspecialidad, int idZona) {
        List<Tecnico> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, e.nombre AS nombre_especialidad, z.nombre AS nombre_zona "
          + "FROM tb_tecnico t "
          + "LEFT JOIN tb_especialidad e ON e.id_especialidad = t.id_especialidad "
          + "LEFT JOIN tb_zona z ON z.id_zona = t.id_zona WHERE t.estado=1 ");
        if (idEspecialidad > 0) sql.append("AND t.id_especialidad=").append(idEspecialidad).append(" ");
        if (idZona > 0)         sql.append("AND t.id_zona=").append(idZona).append(" ");
        sql.append("ORDER BY t.id_tecnico DESC");
        try (PreparedStatement ps = conn().prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] filtrar: " + e.getMessage());
        }
        return lista;
    }

    // ── ESCRITURA ─────────────────────────────────────────────

    public boolean guardar(Tecnico t) {
        String sql = "INSERT INTO tb_tecnico (dni, nombres, apellidos, telefono, email, "
                   + "id_especialidad, id_zona, max_solicitudes_dia, "
                   + "observaciones, estado) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getDni());
            ps.setString(2, t.getNombres());
            ps.setString(3, t.getApellidos());
            ps.setString(4, t.getTelefono());
            ps.setString(5, t.getEmail());
            ps.setInt(6, t.getIdEspecialidad());
            if (t.getIdZona() > 0) ps.setInt(7, t.getIdZona());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, t.getMaxSolicitudesDia());
            ps.setString(9, t.getObservaciones());
            ps.setInt(10, t.getEstado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] guardar: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizar(Tecnico t) {
        String sql = "UPDATE tb_tecnico SET dni=?, nombres=?, apellidos=?, telefono=?, "
                   + "email=?, id_especialidad=?, id_zona=?, "
                   + "max_solicitudes_dia=?, observaciones=?, estado=? WHERE id_tecnico=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getDni());
            ps.setString(2, t.getNombres());
            ps.setString(3, t.getApellidos());
            ps.setString(4, t.getTelefono());
            ps.setString(5, t.getEmail());
            ps.setInt(6, t.getIdEspecialidad());
            if (t.getIdZona() > 0) ps.setInt(7, t.getIdZona());
            else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, t.getMaxSolicitudesDia());
            ps.setString(9, t.getObservaciones());
            ps.setInt(10, t.getEstado());
            ps.setInt(11, t.getIdTecnico());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] actualizar: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE tb_tecnico SET estado=0 WHERE id_tecnico=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] eliminar: " + e.getMessage());
            return false;
        }
    }

    public boolean existeDni(String dni, int idExcluir) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id_tecnico FROM tb_tecnico WHERE dni=? AND id_tecnico<>?")) {
            ps.setString(1, dni); ps.setInt(2, idExcluir);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // ── HORARIO ───────────────────────────────────────────────

    /**
     * Guarda el horario semanal de un técnico (reemplaza el anterior).
     * @param diasActivos  [Lun, Mar, Mié, Jue, Vie, Sáb, Dom]
     * @param horaInicio   "HH:mm"
     * @param horaFin      "HH:mm"
     */
    public boolean guardarHorario(int idTecnico, boolean[] diasActivos, String horaInicio, String horaFin) {
        try {
            try (PreparedStatement ps = conn().prepareStatement(
                    "DELETE FROM tb_horario WHERE id_tecnico=?")) {
                ps.setInt(1, idTecnico);
                ps.executeUpdate();
            }
            String sqlInsert = "INSERT INTO tb_horario (id_tecnico, dia_semana, hora_inicio, hora_fin, activo) VALUES (?,?,?,?,1)";
            try (PreparedStatement ps = conn().prepareStatement(sqlInsert)) {
                for (int dia = 0; dia < 7; dia++) {
                    if (diasActivos[dia]) {
                        ps.setInt(1, idTecnico);
                        ps.setInt(2, dia + 1);
                        ps.setString(3, horaInicio);
                        ps.setString(4, horaFin);
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            return true;
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] guardarHorario: " + e.getMessage());
            return false;
        }
    }

    /** Devuelve booleans[7] indicando días con horario (0=Lun … 6=Dom). */
    public boolean[] cargarDiasHorario(int idTecnico) {
        boolean[] dias = new boolean[7];
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT dia_semana FROM tb_horario WHERE id_tecnico=? AND activo=1")) {
            ps.setInt(1, idTecnico);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int dia = rs.getInt("dia_semana");
                if (dia >= 1 && dia <= 7) dias[dia - 1] = true;
            }
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] cargarDiasHorario: " + e.getMessage());
        }
        // Si el técnico no tiene horario configurado, asumimos que trabaja todos los días
        boolean alguno = false;
        for (boolean d : dias) if (d) { alguno = true; break; }
        if (!alguno) Arrays.fill(dias, true);
        return dias;
    }

    /** Devuelve {"HH:mm", "HH:mm"} con hora_inicio y hora_fin. Default 08:00–17:00. */
    public String[] cargarHorasHorario(int idTecnico) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT hora_inicio, hora_fin FROM tb_horario WHERE id_tecnico=? AND activo=1 LIMIT 1")) {
            ps.setInt(1, idTecnico);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{ rs.getString("hora_inicio"), rs.getString("hora_fin") };
            }
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] cargarHorasHorario: " + e.getMessage());
        }
        return new String[]{"08:00", "17:00"};
    }

    // ── ALGORITMO: OCUPACIÓN POR FECHA ────────────────────────

    /**
     * Devuelve un mapa id_tecnico → lista de bloques ocupados [inicio, fin] para una fecha dada.
     * Un bloque = hora_inicio_asignación … hora_inicio + 3 horas (2 h trabajo + 1 h traslado).
     */
    public Map<Integer, List<LocalTime[]>> obtenerOcupacionPorFecha(LocalDate fecha) {
        Map<Integer, List<LocalTime[]>> mapa = new HashMap<>();
        String sql = "SELECT a.id_tecnico, TIME(a.fecha_programada) AS hora_inicio "
                   + "FROM tb_asignacion a "
                   + "WHERE DATE(a.fecha_programada) = ? "
                   + "  AND a.estado_asignacion NOT IN ('CANCELADA','COMPLETADA')";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idTec = rs.getInt("id_tecnico");
                String hStr = rs.getString("hora_inicio");
                if (hStr == null) continue;
                LocalTime ini = LocalTime.parse(hStr.length() > 5 ? hStr.substring(0, 5) : hStr);
                LocalTime fin = ini.plusHours(3);
                mapa.computeIfAbsent(idTec, k -> new ArrayList<>())
                    .add(new LocalTime[]{ini, fin});
            }
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] obtenerOcupacionPorFecha: " + e.getMessage());
        }
        return mapa;
    }

    /**
     * Cuenta cuántos técnicos activos no tienen ninguna asignación activa hoy.
     */
    public int contarLibresHoy() {
        String sql = "SELECT COUNT(*) FROM tb_tecnico t "
                   + "WHERE t.estado=1 "
                   + "  AND t.id_tecnico NOT IN ("
                   + "      SELECT DISTINCT a.id_tecnico FROM tb_asignacion a "
                   + "      WHERE DATE(a.fecha_programada)=CURDATE() "
                   + "        AND a.estado_asignacion NOT IN ('CANCELADA','COMPLETADA')"
                   + "  )";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            Log.warn("[TecnicoDAO] contarLibresHoy: " + e.getMessage());
        }
        return 0;
    }

    // ── MAPEO ─────────────────────────────────────────────────

    private Tecnico mapear(ResultSet rs) throws SQLException {
        Tecnico t = new Tecnico();
        t.setIdTecnico(rs.getInt("id_tecnico"));
        t.setDni(rs.getString("dni"));
        t.setNombres(rs.getString("nombres"));
        t.setApellidos(rs.getString("apellidos"));
        t.setTelefono(rs.getString("telefono"));
        t.setEmail(rs.getString("email"));
        t.setIdEspecialidad(rs.getInt("id_especialidad"));
        t.setNombreEspecialidad(rs.getString("nombre_especialidad"));
        t.setIdZona(rs.getInt("id_zona"));
        t.setNombreZona(rs.getString("nombre_zona"));
        t.setMaxSolicitudesDia(rs.getInt("max_solicitudes_dia"));
        t.setObservaciones(rs.getString("observaciones"));
        t.setEstado(rs.getInt("estado"));
        return t;
    }
}
