package Controlador;

import DAO.SolicitudDAO;
import Modelo.Cliente;
import Modelo.Solicitud;
import Modelo.TipoServicio;
import Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudController {

    // ── Tabla + filtro ───────────────────────────────────────
    @FXML private TextField  txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Solicitud> tblSolicitudes;
    @FXML private TableColumn<Solicitud,Integer> colId;
    @FXML private TableColumn<Solicitud,String>  colFecha, colHora, colCliente,
                                                  colTipo, colPrioridad, colEstado;
    @FXML private Label lblContador, lblTituloForm, lblMensaje;

    // ── Formulario (sin cmbEstado) ───────────────────────────
    @FXML private ComboBox<Cliente>      cmbCliente;
    @FXML private ComboBox<TipoServicio> cmbTipo;
    @FXML private ComboBox<String>       cmbPrioridad;
    @FXML private DatePicker             dpFechaSolicitud;
    @FXML private TextField              txtHoraHH, txtHoraMM;
    @FXML private TextArea               txtDescripcion, txtObservaciones;
    @FXML private Button                 btnGuardar, btnCancelarSolicitud;

    private final SolicitudDAO dao = new SolicitudDAO();
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

        // Combos formulario — solo PENDIENTE al crear; TERMINADO se pone automáticamente
        cmbPrioridad.setItems(FXCollections.observableArrayList("BAJA","MEDIA","ALTA","CRITICA"));
        cmbPrioridad.setValue("MEDIA");
        dpFechaSolicitud.setValue(LocalDate.now());
        btnCancelarSolicitud.setDisable(true);
        btnGuardar.setDisable(true);

        // Bloquear fechas pasadas en DatePicker
        dpFechaSolicitud.setDayCellFactory(picker -> new DateCell() {
            @Override public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now())
                        || date.isAfter(LocalDate.now().plusMonths(2)));
            }
        });

        // Hora — solo dígitos con validación
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

        cmbCliente.valueProperty().addListener((o, v, n)  -> validarEnTiempoReal());
        cmbTipo.valueProperty().addListener((o, v, n)     -> validarEnTiempoReal());
        cmbPrioridad.valueProperty().addListener((o, v, n)-> validarEnTiempoReal());
        dpFechaSolicitud.valueProperty().addListener((o, v, n) -> validarEnTiempoReal());

        // Filtro de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "PENDIENTE", "ASIGNADA", "COMPLETADA", "CANCELADA"));

        cargarClientes();
        cargarTiposServicio();

        // Auto-completar TERMINADO antes de mostrar la tabla
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
        btnGuardar.setDisable(!ok);
    }

    // ── Auto-TERMINADO ───────────────────────────────────────
    /**
     * Marca como TERMINADO toda solicitud cuyo estado sea PENDIENTE o ASIGNADA,
     * cuya fecha sea hoy (o anterior) y cuya hora + 2 h ya haya pasado.
     * También cierra asignaciones activas asociadas.
     */
    private void actualizarTerminadas() {
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<Solicitud> activas = dao.listarParaAutoTerminar(); // estado IN (PENDIENTE, ASIGNADA, EN_CAMINO, EN_PROCESO)
        for (Solicitud s : activas) {
            if (s.getFechaSolicitada() == null) continue;
            // Solo las de hoy o días anteriores
            if (s.getFechaSolicitada().isAfter(hoy)) continue;

            LocalTime horaSol = parsearHora(s.getHorarioPreferido());
            if (horaSol == null) continue;

            // Si la fecha es anterior a hoy → siempre terminado
            boolean pasado = s.getFechaSolicitada().isBefore(hoy);
            // Si es hoy → solo si hora + 2h ya pasó
            boolean horaVencida = pasado || ahora.isAfter(horaSol.plusHours(2));

            if (horaVencida) {
                dao.cambiarEstado(s.getIdSolicitud(), "TERMINADO");
            }
        }
    }

    private LocalTime parsearHora(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try { return LocalTime.parse(hhmm.trim().length() == 5 ? hhmm.trim() : hhmm.trim().substring(0,5)); }
        catch (Exception e) { return null; }
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
    @FXML public void nuevaSolicitud() { limpiarFormulario(); }

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
                || (s.getCodigo()        != null && s.getCodigo().toLowerCase().contains(txt))
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

        // Habilitar cancelar solo si aún está pendiente
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

        Solicitud s = new Solicitud();
        s.setIdCliente(cmbCliente.getValue().getIdCliente());
        s.setIdTipoServicio(cmbTipo.getValue().getIdTipoServicio());
        s.setPrioridad(cmbPrioridad.getValue());
        // El estado siempre es PENDIENTE al crear; al editar se conserva el existente
        s.setEstado(solicitudSeleccionada == null ? "PENDIENTE" : solicitudSeleccionada.getEstado());
        s.setFechaSolicitada(dpFechaSolicitud.getValue());
        s.setHorarioPreferido(obtenerHora());
        s.setDescripcion(txtDescripcion.getText().trim());
        s.setObservaciones(txtObservaciones.getText().trim());

        boolean ok;
        if (solicitudSeleccionada == null) {
            ok = dao.guardar(s);
        } else {
            s.setIdSolicitud(solicitudSeleccionada.getIdSolicitud());
            ok = dao.actualizar(s);
        }

        if (ok) {
            mostrarMensaje(solicitudSeleccionada == null ? "Solicitud registrada." : "Solicitud actualizada.", true);
            actualizarTerminadas();
            cargarTabla();
            limpiarFormulario();
        } else {
            mostrarMensaje("Error al guardar.", false);
        }
    }

    // ── Cancelar solicitud ───────────────────────────────────
    @FXML public void cancelarSolicitud() {
        if (solicitudSeleccionada == null) return;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Cancelar la solicitud " + solicitudSeleccionada.getCodigo() + "?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES && dao.cambiarEstado(solicitudSeleccionada.getIdSolicitud(), "CANCELADA")) {
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
        if (fecha == null) return "Selecciona la fecha de atención.";
        if (fecha.isBefore(LocalDate.now()))               return "La fecha no puede ser anterior a hoy.";
        if (fecha.isAfter(LocalDate.now().plusMonths(2)))  return "La fecha no puede ser más de 2 meses adelante.";
        if (txtHoraHH != null) {
            String hh = txtHoraHH.getText().trim(), mm = txtHoraMM.getText().trim();
            if (hh.isEmpty() || mm.isEmpty()) return "Ingresa la hora de atención.";
            int total = Integer.parseInt(hh) * 60 + Integer.parseInt(mm);
            if (total < 480)  return "La hora no puede ser antes de las 08:00.";
            if (total > 1020) return "La hora no puede ser después de las 17:00.";
        }
        return null;
    }

    private void mostrarMensaje(String t, boolean ok) {
        lblMensaje.setText(t);
        lblMensaje.setStyle(ok ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
                               : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
    }
    private String nvl(String s) { return s == null ? "" : s; }
}
