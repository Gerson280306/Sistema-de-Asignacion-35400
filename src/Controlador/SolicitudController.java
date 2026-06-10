package Controlador;

import DAO.SolicitudDAO;
import DAO.TecnicoDAO;
import Modelo.Cliente;
import Modelo.Solicitud;
import Modelo.Tecnico;
import Modelo.TipoServicio;
import Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolicitudController {

    // ── Tabla + filtro ───────────────────────────────────────
    @FXML private TextField  txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Solicitud> tblSolicitudes;
    @FXML private TableColumn<Solicitud,Integer> colId;
    @FXML private TableColumn<Solicitud,String>  colFecha, colHora, colCliente,
                                                  colTipo, colPrioridad, colEstado;
    @FXML private Label lblContador, lblTituloForm, lblMensaje;
    @FXML private Label lblDisponibilidad;

    // ── Formulario — orden: Cliente, Tipo, Prioridad, Fecha del servicio, Hora, Descripcion, Observaciones
    @FXML private ComboBox<Cliente>      cmbCliente;
    @FXML private ComboBox<TipoServicio> cmbTipo;
    @FXML private ComboBox<String>       cmbPrioridad;
    @FXML private DatePicker             dpFechaSolicitud;   // label en FXML: "Fecha del servicio"
    @FXML private TextField              txtHoraHH, txtHoraMM;
    @FXML private TextArea               txtDescripcion, txtObservaciones;
    @FXML private Button                 btnGuardar, btnCancelarSolicitud;

    @FXML private StackPane toastPane;

    private final SolicitudDAO dao        = new SolicitudDAO();
    private final TecnicoDAO   tecnicoDAO = new TecnicoDAO();
    private Solicitud solicitudSeleccionada = null;
    private ObservableList<Solicitud> todasLasSolicitudes = FXCollections.observableArrayList();

    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        // Columnas tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colFecha.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getFechaSolicitada() != null ? d.getValue().getFechaSolicitada().toString() : ""));
        if (colHora != null)
            colHora.setCellValueFactory(new PropertyValueFactory<>("horarioPreferido"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("nombreTipo"));
        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Combos
        cmbPrioridad.setItems(FXCollections.observableArrayList("BAJA","MEDIA","ALTA","CRITICA"));
        cmbPrioridad.setValue("MEDIA");
        dpFechaSolicitud.setValue(LocalDate.now());
        btnCancelarSolicitud.setDisable(true);
        btnGuardar.setDisable(true);

        // Bloquear fechas pasadas y más de 2 meses en DatePicker
        dpFechaSolicitud.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())
                        || date.isAfter(LocalDate.now().plusMonths(2)));
            }
        });

        // Hora — solo dígitos
        if (txtHoraHH != null) {
            txtHoraHH.setText("08"); txtHoraMM.setText("00");
            txtHoraHH.textProperty().addListener((o, v, n) -> {
                if (!n.matches("\\d{0,2}")) { txtHoraHH.setText(v); return; }
                if (!n.isEmpty() && Integer.parseInt(n) > 23) txtHoraHH.setText("23");
                validarEnTiempoReal();
            });
            txtHoraMM.textProperty().addListener((o, v, n) -> {
                if (!n.matches("\\d{0,2}")) { txtHoraMM.setText(v); return; }
                if (!n.isEmpty() && Integer.parseInt(n) > 59) txtHoraMM.setText("59");
                validarEnTiempoReal();
            });
        }

        cmbCliente.valueProperty().addListener((o, v, n)       -> validarEnTiempoReal());
        // Al cambiar tipo: validar especialidad disponible + disponibilidad general
        cmbTipo.valueProperty().addListener((o, v, n)          -> { verificarEspecialidadTipo(n); validarEnTiempoReal(); });
        cmbPrioridad.valueProperty().addListener((o, v, n)     -> validarEnTiempoReal());
        dpFechaSolicitud.valueProperty().addListener((o, v, n) -> validarEnTiempoReal());

        // Filtro estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "PENDIENTE", "ASIGNADA", "COMPLETADA", "CANCELADA"));

        cargarClientes();
        cargarTiposServicio();
        actualizarTerminadas();
        cargarTabla();
    }

    // ── Validación en tiempo real ────────────────────────────
    private void validarEnTiempoReal() {
        boolean ok = cmbCliente.getValue() != null
                  && cmbTipo.getValue() != null
                  && cmbPrioridad.getValue() != null
                  && dpFechaSolicitud.getValue() != null;
        if (ok && txtHoraHH != null)
            ok = !txtHoraHH.getText().trim().isEmpty() && !txtHoraMM.getText().trim().isEmpty();

        if (ok && txtHoraHH != null) {
            LocalDate fecha = dpFechaSolicitud.getValue();
            LocalTime hora  = parsearHora(txtHoraHH.getText() + ":" + txtHoraMM.getText());

            // Validar hora no pasada si la fecha es hoy
            if (fecha != null && hora != null && fecha.equals(LocalDate.now())) {
                if (hora.isBefore(LocalTime.now())) {
                    mostrarAviso("⚠️ La hora ingresada ya pasó. Elige una hora futura.", false);
                    btnGuardar.setDisable(true);
                    return;
                }
            }

            // Validar disponibilidad por especialidad + horaria
            if (fecha != null && hora != null && cmbTipo.getValue() != null) {
                String aviso = verificarDisponibilidad(fecha, hora, cmbTipo.getValue().getIdTipoServicio());
                if (aviso != null) {
                    mostrarAviso(aviso, false);
                    btnGuardar.setDisable(true);
                    return;
                }
            }
            mostrarAviso("", true);
        }
        btnGuardar.setDisable(!ok);
    }

    /**
     * Al seleccionar un tipo de servicio, verifica si existe al menos un técnico
     * activo con la especialidad requerida. Si no hay ninguno, muestra advertencia.
     */
    private void verificarEspecialidadTipo(TipoServicio tipo) {
        if (tipo == null) { mostrarAviso("", true); return; }
        int idEspecialidad = dao.obtenerEspecialidadDeTipo(tipo.getIdTipoServicio());
        if (idEspecialidad <= 0) return;
        boolean hayTecnico = hayTecnicoConEspecialidad(idEspecialidad);
        if (!hayTecnico) {
            mostrarAviso("⚠️ No hay técnicos activos con la especialidad requerida para este servicio.", false);
        }
    }

    /** Devuelve true si hay al menos un técnico activo con esa especialidad. */
    private boolean hayTecnicoConEspecialidad(int idEspecialidad) {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(
                "SELECT COUNT(*) FROM tb_tecnico WHERE estado=1 AND id_especialidad=?")) {
            ps.setInt(1, idEspecialidad);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[SolicitudController] hayTecnicoConEspecialidad: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica disponibilidad de técnicos con la especialidad del tipo seleccionado
     * para la fecha/hora dada. Retorna mensaje de error o null si hay espacio.
     */
    private String verificarDisponibilidad(LocalDate fecha, LocalTime hora, int idTipoServicio) {
        int idEspecialidad = dao.obtenerEspecialidadDeTipo(idTipoServicio);
        int diaSemana = fecha.getDayOfWeek().getValue();

        // Solo técnicos activos con la especialidad requerida
        List<Tecnico> activos = idEspecialidad > 0
                ? tecnicoDAO.filtrar(idEspecialidad, 0)
                : tecnicoDAO.listarActivos();

        if (activos.isEmpty()) {
            return "❌ No hay técnicos activos con la especialidad requerida para este servicio.";
        }

        Map<Integer, List<LocalTime[]>> ocupadosPorTecnico =
                tecnicoDAO.obtenerOcupacionPorFecha(fecha);
        List<LocalTime> pendientesSinAsignar =
                dao.listarHorasPendientesPorFecha(fecha);

        int capacidad = 0;
        for (Tecnico t : activos) {
            boolean[] dias = tecnicoDAO.cargarDiasHorario(t.getIdTecnico());
            if (!dias[diaSemana - 1]) continue;
            String[] horas = tecnicoDAO.cargarHorasHorario(t.getIdTecnico());
            LocalTime ini = parsearHora(horas[0]);
            LocalTime fin = parsearHora(horas[1]);
            if (ini == null) ini = LocalTime.of(8,0);
            if (fin == null) fin = LocalTime.of(17,0);
            if (hora.isBefore(ini) || hora.isAfter(fin.minusHours(2))) continue;
            List<LocalTime[]> bloques = ocupadosPorTecnico.getOrDefault(t.getIdTecnico(), new ArrayList<>());
            boolean solapado = false;
            for (LocalTime[] b : bloques) {
                if (hora.isBefore(b[1]) && hora.plusHours(3).isAfter(b[0])) { solapado = true; break; }
            }
            if (!solapado) capacidad++;
        }

        long pendientesEnBloque = pendientesSinAsignar.stream().filter(h ->
            hora.isBefore(h.plusHours(3)) && hora.plusHours(3).isAfter(h)
        ).count();

        int libres = (int)(capacidad - pendientesEnBloque);

        if (libres <= 0) {
            LocalTime limiteMaximo = LocalTime.of(17, 0);
            LocalTime huecoAntes = null;
            LocalTime candidato = LocalTime.of(8, 0);
            while (!candidato.isAfter(hora.minusMinutes(30))) {
                if (verificarHoraLibre(fecha, candidato, activos,
                        ocupadosPorTecnico, pendientesSinAsignar, diaSemana)) {
                    huecoAntes = candidato; break;
                }
                candidato = candidato.plusMinutes(30);
            }
            LocalTime proxima = null;
            candidato = hora.plusMinutes(30);
            while (!candidato.isAfter(limiteMaximo)) {
                if (verificarHoraLibre(fecha, candidato, activos,
                        ocupadosPorTecnico, pendientesSinAsignar, diaSemana)) {
                    proxima = candidato; break;
                }
                candidato = candidato.plusMinutes(30);
            }
            StringBuilder msg = new StringBuilder(
                "❌ Sin técnicos disponibles el " + fecha + " a las " + hora.toString().substring(0,5) + ".");
            if (huecoAntes != null)
                msg.append(" Disponibilidad antes: ").append(huecoAntes.toString().substring(0,5)).append(".");
            if (proxima != null)
                msg.append(" Próxima hora: ").append(proxima.toString().substring(0,5)).append(".");
            if (huecoAntes == null && proxima == null)
                msg.append(" No hay horarios disponibles ese día.");
            return msg.toString();
        }
        return null;
    }

    private boolean verificarHoraLibre(LocalDate fecha, LocalTime hora,
            List<Tecnico> activos,
            Map<Integer, List<LocalTime[]>> ocupadosPorTecnico,
            List<LocalTime> pendientesSinAsignar,
            int diaSemana) {
        int capacidad = 0;
        for (Tecnico t : activos) {
            boolean[] dias = tecnicoDAO.cargarDiasHorario(t.getIdTecnico());
            if (!dias[diaSemana - 1]) continue;
            String[] hs = tecnicoDAO.cargarHorasHorario(t.getIdTecnico());
            LocalTime ini = parsearHora(hs[0]); LocalTime fin = parsearHora(hs[1]);
            if (ini == null) ini = LocalTime.of(8,0);
            if (fin == null) fin = LocalTime.of(17,0);
            if (hora.isBefore(ini) || hora.isAfter(fin.minusHours(2))) continue;
            List<LocalTime[]> bloques = ocupadosPorTecnico.getOrDefault(t.getIdTecnico(), new ArrayList<>());
            boolean solapado = false;
            for (LocalTime[] b : bloques)
                if (hora.isBefore(b[1]) && hora.plusHours(3).isAfter(b[0])) { solapado=true; break; }
            if (!solapado) capacidad++;
        }
        long pendientesEnBloque = pendientesSinAsignar.stream().filter(h ->
            hora.isBefore(h.plusHours(3)) && hora.plusHours(3).isAfter(h)
        ).count();
        return (capacidad - pendientesEnBloque) > 0;
    }

    private void mostrarAviso(String msg, boolean ok) {
        if (lblDisponibilidad != null) {
            lblDisponibilidad.setText(msg);
            lblDisponibilidad.setStyle(ok
                ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
                : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
        }
    }

    // ── Auto-COMPLETADO ──────────────────────────────────────
    private void actualizarTerminadas() {
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        List<Solicitud> activas = dao.listarParaAutoTerminar();
        for (Solicitud s : activas) {
            if (s.getFechaSolicitada() == null) continue;
            if (s.getFechaSolicitada().isAfter(hoy)) continue;
            LocalTime horaSol = parsearHora(s.getHorarioPreferido());
            if (horaSol == null) continue;
            boolean pasado    = s.getFechaSolicitada().isBefore(hoy);
            boolean horaVencida = pasado || ahora.isAfter(horaSol.plusHours(2));
            if (horaVencida) dao.cambiarEstado(s.getIdSolicitud(), "COMPLETADA");
        }
    }

    // ── Carga de combos ──────────────────────────────────────
    private void cargarClientes() {
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_cliente, nombres, apellidos FROM tb_cliente WHERE estado=1 ORDER BY nombres");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setIdCliente(rs.getInt(1));
                c.setNombres(rs.getString(2));
                c.setApellidos(rs.getString(3));
                lista.add(c);
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        cmbCliente.setItems(FXCollections.observableArrayList(lista));
    }

    private void cargarTiposServicio() {
        List<TipoServicio> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tipo_servicio, nombre FROM tb_tipo_servicio WHERE estado=1 ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new TipoServicio(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        cmbTipo.setItems(FXCollections.observableArrayList(lista));
    }

    // ── Tabla ────────────────────────────────────────────────
    private void cargarTabla() {
        todasLasSolicitudes = FXCollections.observableArrayList(dao.listarTodos());
        tblSolicitudes.setItems(todasLasSolicitudes);
        lblContador.setText(todasLasSolicitudes.size() + " solicitudes");
    }

    // ── Filtros ──────────────────────────────────────────────
    @FXML public void nuevaSolicitud()  { limpiarFormulario(); }
    @FXML public void buscarSolicitud() { aplicarFiltros(); }

    @FXML public void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroEstado.setValue(null);
        tblSolicitudes.setItems(todasLasSolicitudes);
        lblContador.setText(todasLasSolicitudes.size() + " solicitudes");
    }

    private void aplicarFiltros() {
        String txt    = txtBuscar.getText().trim().toLowerCase();
        String estado = cmbFiltroEstado.getValue();
        if (txt.isEmpty() && estado == null) {
            tblSolicitudes.setItems(todasLasSolicitudes);
            lblContador.setText(todasLasSolicitudes.size() + " solicitudes");
            return;
        }
        ObservableList<Solicitud> filtradas = FXCollections.observableArrayList();
        for (Solicitud s : todasLasSolicitudes) {
            boolean matchTexto = txt.isEmpty()
                || (s.getNombreCliente() != null && s.getNombreCliente().toLowerCase().contains(txt))
                || (s.getNombreTipo()    != null && s.getNombreTipo().toLowerCase().contains(txt));
            boolean matchEstado = estado == null || estado.equals(s.getEstado());
            if (matchTexto && matchEstado) filtradas.add(s);
        }
        tblSolicitudes.setItems(filtradas);
        lblContador.setText(filtradas.size() + " solicitudes encontradas");
    }

    // ── Selección de fila ────────────────────────────────────
    @FXML public void seleccionarSolicitud() {
        Solicitud sel = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        solicitudSeleccionada = sel;
        lblTituloForm.setText("Editar solicitud");

        boolean cancelable = "PENDIENTE".equals(sel.getEstado());
        btnCancelarSolicitud.setDisable(!cancelable);

        cmbCliente.getItems().stream()
            .filter(c -> c.getIdCliente() == sel.getIdCliente())
            .findFirst().ifPresent(cmbCliente::setValue);
        cmbTipo.getItems().stream()
            .filter(t -> t.getIdTipoServicio() == sel.getIdTipoServicio())
            .findFirst().ifPresent(cmbTipo::setValue);
        cmbPrioridad.setValue(sel.getPrioridad());
        if (sel.getFechaSolicitada() != null) dpFechaSolicitud.setValue(sel.getFechaSolicitada());
        txtDescripcion.setText(nvl(sel.getDescripcion()));
        txtObservaciones.setText(nvl(sel.getObservaciones()));
        String hora = sel.getHorarioPreferido();
        if (txtHoraHH != null && hora != null && hora.contains(":")) {
            txtHoraHH.setText(hora.split(":")[0]);
            txtHoraMM.setText(hora.split(":")[1]);
        }
    }

    // ── Guardar ──────────────────────────────────────────────
    @FXML public void guardarSolicitud() {
        String error = validarCompleto();
        if (error != null) { mostrarMensaje(error, false); return; }

        Cliente      cliente = cmbCliente.getValue();
        TipoServicio tipo    = cmbTipo.getValue();
        LocalDate    fecha   = dpFechaSolicitud.getValue();
        String       hora    = obtenerHora();

        // Validar duplicado: mismo cliente, misma fecha y misma hora
        boolean esEdicion = (solicitudSeleccionada != null);
        int idExcluir = esEdicion ? solicitudSeleccionada.getIdSolicitud() : 0;
        if (existeSolicitudDuplicada(cliente.getIdCliente(), fecha, hora, idExcluir)) {
            mostrarMensaje("❌ Este cliente ya tiene una solicitud para esa fecha y hora.", false);
            return;
        }

        // Validar que haya técnico con la especialidad correcta disponible
        String avisoDisp = verificarDisponibilidad(fecha, parsearHora(hora), tipo.getIdTipoServicio());
        if (avisoDisp != null) {
            mostrarMensaje(avisoDisp, false);
            return;
        }

        Solicitud s = new Solicitud();
        s.setIdCliente(cliente.getIdCliente());
        s.setIdTipoServicio(tipo.getIdTipoServicio());
        s.setPrioridad(cmbPrioridad.getValue());
        s.setEstado(esEdicion ? solicitudSeleccionada.getEstado() : "PENDIENTE");
        s.setFechaSolicitada(fecha);
        s.setHorarioPreferido(hora);
        s.setDescripcion(txtDescripcion.getText().trim());
        s.setObservaciones(txtObservaciones.getText().trim());

        boolean ok;
        if (!esEdicion) {
            ok = dao.guardar(s);
        } else {
            s.setIdSolicitud(solicitudSeleccionada.getIdSolicitud());
            ok = dao.actualizar(s);
        }

        if (ok) {
            mostrarToast(!esEdicion ? "✔  Solicitud nueva registrada" : "✔  Solicitud actualizada", true);
            mostrarMensaje(!esEdicion ? "Solicitud registrada." : "Solicitud actualizada.", true);
            actualizarTerminadas();
            cargarTabla();
            limpiarFormulario();
        } else {
            mostrarMensaje("Error al guardar.", false);
        }
    }

    /**
     * Verifica si ya existe una solicitud activa (no cancelada) para el mismo
     * cliente, fecha y hora. Excluye la solicitud que se está editando.
     */
    private boolean existeSolicitudDuplicada(int idCliente, LocalDate fecha, String hora, int idExcluir) {
        String sql = "SELECT COUNT(*) FROM tb_solicitud " +
                     "WHERE id_cliente=? AND fecha_solicitada=? AND horario_preferido=? " +
                     "  AND estado != 'CANCELADA' " +
                     (idExcluir > 0 ? "AND id_solicitud != ? " : "");
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setDate(2, java.sql.Date.valueOf(fecha));
            ps.setString(3, hora);
            if (idExcluir > 0) ps.setInt(4, idExcluir);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[SolicitudController] existeSolicitudDuplicada: " + e.getMessage());
        }
        return false;
    }

    // ── Cancelar solicitud ───────────────────────────────────
    @FXML public void cancelarSolicitud() {
        if (solicitudSeleccionada == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Cancelar la solicitud #" + solicitudSeleccionada.getIdSolicitud() + "?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES && dao.cambiarEstado(solicitudSeleccionada.getIdSolicitud(), "CANCELADA")) {
                mostrarToast("✔  Solicitud cancelada", true);
                mostrarMensaje("Solicitud cancelada.", true);
                actualizarTerminadas();
                cargarTabla();
                limpiarFormulario();
            }
        });
    }

    // ── Atajos de hora ───────────────────────────────────────
    @FXML public void setHora0800() { if (txtHoraHH != null) { txtHoraHH.setText("08"); txtHoraMM.setText("00"); } }
    @FXML public void setHora1200() { if (txtHoraHH != null) { txtHoraHH.setText("12"); txtHoraMM.setText("00"); } }
    @FXML public void setHora1500() { if (txtHoraHH != null) { txtHoraHH.setText("15"); txtHoraMM.setText("00"); } }
    @FXML public void setHora1700() { if (txtHoraHH != null) { txtHoraHH.setText("17"); txtHoraMM.setText("00"); } }

    // ── Limpiar formulario ───────────────────────────────────
    @FXML public void limpiarFormulario() {
        solicitudSeleccionada = null;
        cmbCliente.setValue(null); cmbTipo.setValue(null);
        cmbPrioridad.setValue("MEDIA");
        dpFechaSolicitud.setValue(LocalDate.now());
        if (txtHoraHH != null) { txtHoraHH.setText("08"); txtHoraMM.setText("00"); }
        txtDescripcion.clear(); txtObservaciones.clear();
        lblMensaje.setText(""); lblTituloForm.setText("Nueva solicitud");
        if (lblDisponibilidad != null) lblDisponibilidad.setText("");
        btnCancelarSolicitud.setDisable(true); btnGuardar.setDisable(true);
        tblSolicitudes.getSelectionModel().clearSelection();
    }

    // ── Privados ─────────────────────────────────────────────
    private String obtenerHora() {
        if (txtHoraHH == null || txtHoraHH.getText().trim().isEmpty()) return "08:00";
        return String.format("%02d:%02d",
            Integer.parseInt(txtHoraHH.getText().trim()),
            Integer.parseInt(txtHoraMM.getText().trim()));
    }

    private String validarCompleto() {
        if (cmbCliente.getValue() == null)   return "Selecciona el cliente.";
        if (cmbTipo.getValue() == null)      return "Selecciona el tipo de servicio.";
        if (cmbPrioridad.getValue() == null) return "Selecciona la prioridad.";
        LocalDate fecha = dpFechaSolicitud.getValue();
        if (fecha == null)                              return "Selecciona la fecha del servicio.";
        if (fecha.isBefore(LocalDate.now()))            return "La fecha no puede ser anterior a hoy.";
        if (fecha.isAfter(LocalDate.now().plusMonths(2))) return "La fecha no puede ser más de 2 meses adelante.";
        if (txtHoraHH != null) {
            String hh = txtHoraHH.getText().trim(), mm = txtHoraMM.getText().trim();
            if (hh.isEmpty() || mm.isEmpty()) return "Ingresa la hora de atención.";
            int total = Integer.parseInt(hh) * 60 + Integer.parseInt(mm);
            if (total < 480)  return "La hora no puede ser antes de las 08:00.";
            if (total > 1020) return "La hora no puede ser después de las 17:00.";
        }
        // Validar especialidad disponible al guardar
        int idEsp = dao.obtenerEspecialidadDeTipo(cmbTipo.getValue().getIdTipoServicio());
        if (idEsp > 0 && !hayTecnicoConEspecialidad(idEsp))
            return "❌ No hay técnicos activos con la especialidad requerida para este servicio.";
        return null;
    }

    private LocalTime parsearHora(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try { return LocalTime.parse(hhmm.trim().length() == 5 ? hhmm.trim() : hhmm.trim().substring(0,5)); }
        catch (Exception e) { return null; }
    }

    private void mostrarMensaje(String t, boolean ok) {
        lblMensaje.setText(t);
        lblMensaje.setStyle(ok ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
                               : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private void mostrarToast(String mensaje, boolean exito) {
        if (toastPane == null) return;
        Label toast = new Label(mensaje);
        toast.setWrapText(false);
        toast.setStyle(
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;" +
            "-fx-padding: 10 20 10 20; -fx-background-radius: 18;" +
            (exito ? "-fx-background-color: #2E7D32;" : "-fx-background-color: #C62828;") +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 8, 0, 0, 2);"
        );
        StackPane.setAlignment(toast, Pos.CENTER);
        toastPane.getChildren().add(toast);
        FadeTransition fadeIn  = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);
        PauseTransition pausa  = new PauseTransition(Duration.seconds(2.2));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), toast);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        SequentialTransition seq = new SequentialTransition(fadeIn, pausa, fadeOut);
        seq.setOnFinished(e -> toastPane.getChildren().remove(toast));
        seq.play();
    }
}
