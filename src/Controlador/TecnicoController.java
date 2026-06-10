package Controlador;

import DAO.TecnicoDAO;
import Modelo.Tecnico;
import Modelo.Especialidad;
import Modelo.Zona;
import Conexion.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TecnicoController {

    // ── Tabla + filtros ──────────────────────────────────────
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Especialidad> cmbFiltroEspecialidad;
    @FXML private ComboBox<Zona>         cmbFiltroDistrito;
    @FXML private TableView<Tecnico>     tblTecnicos;
    @FXML private TableColumn<Tecnico,Integer> colId;
    @FXML private TableColumn<Tecnico,String>  colDni, colNombre, colEspecialidad, colDistrito, colEstado;
    @FXML private Label lblContador, lblDisponibles, lblTituloForm, lblMensaje;

    // ── Formulario (sin cmbDisponibilidad) ───────────────────
    @FXML private TextField        txtDni, txtNombres, txtApellidos, txtTelefono, txtEmail;
    @FXML private ComboBox<Especialidad> cmbEspecialidad;
    @FXML private ComboBox<Zona>         cmbDistrito;
    @FXML private TextField        txtInicioHH, txtInicioMM, txtFinHH, txtFinMM;
    @FXML private Button           btnInicio0700, btnInicio0800, btnFin1700, btnFin1800;
    @FXML private CheckBox         chkLunes, chkMartes, chkMiercoles, chkJueves,
                                    chkViernes, chkSabado, chkDomingo;
    @FXML private Spinner<Integer> spMaxAsignaciones;
    @FXML private Button           btnGuardar, btnEliminar;

    /** StackPane raíz del FXML — necesario para el toast */
    @FXML private StackPane toastPane;

    private final TecnicoDAO dao = new TecnicoDAO();
    private Tecnico tecnicoSeleccionado = null;
    private ObservableList<Tecnico> todosLosTecnicos = FXCollections.observableArrayList();

    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        colId          .setCellValueFactory(new PropertyValueFactory<>("idTecnico"));
        colDni         .setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre      .setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colEspecialidad.setCellValueFactory(new PropertyValueFactory<>("nombreEspecialidad"));
        if (colDistrito != null)
            colDistrito.setCellValueFactory(new PropertyValueFactory<>("nombreZona"));
        colEstado.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            d.getValue().getEstado() == 1 ? "Activo" : "Inactivo"));

        btnEliminar.setDisable(true);
        btnGuardar.setDisable(true);
        spMaxAsignaciones.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 6));

        // Hora inicio
        txtInicioHH.setText("08"); txtInicioMM.setText("00");
        txtInicioHH.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d{0,2}")) { txtInicioHH.setText(v); return; }
            if (!n.isEmpty() && Integer.parseInt(n) > 19) txtInicioHH.setText("19");
            if (!n.isEmpty() && Integer.parseInt(n) < 8)  txtInicioHH.setText("08");
        });
        txtInicioMM.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d{0,2}")) { txtInicioMM.setText(v); return; }
            if (!n.isEmpty() && Integer.parseInt(n) > 59) txtInicioMM.setText("59");
        });
        txtInicioHH.focusedProperty().addListener((o, was, is) -> {
            if (!is && !txtInicioHH.getText().trim().isEmpty()) {
                int h = Integer.parseInt(txtInicioHH.getText().trim());
                if (h < 8)  txtInicioHH.setText("08");
                if (h > 19) txtInicioHH.setText("19");
            }
        });

        // Hora fin
        txtFinHH.setText("17"); txtFinMM.setText("00");
        txtFinHH.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d{0,2}")) { txtFinHH.setText(v); return; }
            if (!n.isEmpty() && Integer.parseInt(n) > 19) txtFinHH.setText("19");
        });
        txtFinMM.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d{0,2}")) { txtFinMM.setText(v); return; }
            if (!n.isEmpty() && Integer.parseInt(n) > 59) txtFinMM.setText("59");
        });
        txtFinHH.focusedProperty().addListener((o, was, is) -> {
            if (!is && !txtFinHH.getText().trim().isEmpty()) {
                int h = Integer.parseInt(txtFinHH.getText().trim());
                if (h < 8)  txtFinHH.setText("08");
                if (h > 19) txtFinHH.setText("19");
            }
        });

        btnInicio0700.setOnAction(e -> { txtInicioHH.setText("09"); txtInicioMM.setText("00"); });
        btnInicio0800.setOnAction(e -> { txtInicioHH.setText("08"); txtInicioMM.setText("00"); });
        btnFin1700.setOnAction(e ->   { txtFinHH.setText("17");   txtFinMM.setText("00"); });
        btnFin1800.setOnAction(e ->   { txtFinHH.setText("18");   txtFinMM.setText("00"); });

        txtDni.textProperty().addListener((o, v, n) -> {
            if (!n.matches("\\d*")) txtDni.setText(n.replaceAll("[^\\d]",""));
            validarEnTiempoReal();
        });
        txtTelefono.textProperty().addListener((o, v, n) -> {
            if (!n.matches("[\\d ]*")) txtTelefono.setText(n.replaceAll("[^\\d ]",""));
        });
        txtNombres.textProperty().addListener((o, v, n)   -> validarEnTiempoReal());
        txtApellidos.textProperty().addListener((o, v, n) -> validarEnTiempoReal());
        cmbEspecialidad.valueProperty().addListener((o, v, n) -> validarEnTiempoReal());

        cargarEspecialidades();
        cargarDistritos();
        cargarTabla();
    }

    private void validarEnTiempoReal() {
        btnGuardar.setDisable(
            txtDni.getText().trim().isEmpty()
         || txtNombres.getText().trim().isEmpty()
         || txtApellidos.getText().trim().isEmpty()
         || cmbEspecialidad.getValue() == null
        );
    }

    // ── Carga de combos ──────────────────────────────────────
    private void cargarEspecialidades() {
        List<Especialidad> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_especialidad, nombre FROM tb_especialidad WHERE estado=1 ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Especialidad(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        ObservableList<Especialidad> items = FXCollections.observableArrayList(lista);
        cmbEspecialidad.setItems(items);
        cmbFiltroEspecialidad.setItems(items);
    }

    private void cargarDistritos() {
        List<Zona> lista = new ArrayList<>();
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement(
                    "SELECT id_zona, nombre FROM tb_zona " +
                    "WHERE estado=1 AND nombre IN ('San Bartolo','Punta Negra','Punta Hermoso','Pucusana') " +
                    "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Zona(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        ObservableList<Zona> items = FXCollections.observableArrayList(lista);
        cmbDistrito.setItems(items);
        cmbFiltroDistrito.setItems(items);
    }

    // ── Tabla ────────────────────────────────────────────────
    private void cargarTabla() {
        todosLosTecnicos = FXCollections.observableArrayList(dao.listarTodos());
        aplicarFiltros();
    }

    @FXML public void nuevoTecnico()   { limpiarFormulario(); }
    @FXML public void buscarTecnico()  { aplicarFiltros(); }
    @FXML public void filtrar()        { aplicarFiltros(); }

    private void aplicarFiltros() {
        String     txt  = txtBuscar.getText().trim().toLowerCase();
        Especialidad esp = cmbFiltroEspecialidad.getValue();
        Zona         dist = cmbFiltroDistrito.getValue();

        ObservableList<Tecnico> filtrados = FXCollections.observableArrayList();
        for (Tecnico t : todosLosTecnicos) {
            boolean ok = true;
            if (!txt.isEmpty())
                ok = t.getNombreCompleto().toLowerCase().contains(txt)
                  || (t.getDni() != null && t.getDni().contains(txt))
                  || (t.getNombreEspecialidad() != null && t.getNombreEspecialidad().toLowerCase().contains(txt));
            if (ok && esp  != null) ok = t.getIdEspecialidad() == esp.getIdEspecialidad();
            if (ok && dist != null) ok = t.getIdZona()         == dist.getIdZona();
            if (ok) filtrados.add(t);
        }
        tblTecnicos.setItems(filtrados);
        lblContador.setText(filtrados.size() + " técnicos");
        // lblDisponibles queda vacío — disponibilidad ya no se gestiona aquí
        if (lblDisponibles != null) lblDisponibles.setText("");
    }

    // ── Selección ────────────────────────────────────────────
    @FXML public void seleccionarTecnico() {
        Tecnico sel = tblTecnicos.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        tecnicoSeleccionado = sel;
        lblTituloForm.setText("Editar técnico");
        actualizarBoton(sel.getEstado() == 1);
        btnEliminar.setDisable(false);

        txtDni.setText(nvl(sel.getDni()));
        txtNombres.setText(nvl(sel.getNombres()));
        txtApellidos.setText(nvl(sel.getApellidos()));
        txtTelefono.setText(nvl(sel.getTelefono()));
        txtEmail.setText(nvl(sel.getEmail()));
        spMaxAsignaciones.getValueFactory().setValue(sel.getMaxSolicitudesDia() > 0 ? sel.getMaxSolicitudesDia() : 6);

        cmbEspecialidad.getItems().stream()
            .filter(e -> e.getIdEspecialidad() == sel.getIdEspecialidad())
            .findFirst().ifPresent(cmbEspecialidad::setValue);
        cmbDistrito.getItems().stream()
            .filter(z -> z.getIdZona() == sel.getIdZona())
            .findFirst().ifPresent(cmbDistrito::setValue);

        boolean[] dias = dao.cargarDiasHorario(sel.getIdTecnico());
        chkLunes.setSelected(dias[0]); chkMartes.setSelected(dias[1]);
        chkMiercoles.setSelected(dias[2]); chkJueves.setSelected(dias[3]);
        chkViernes.setSelected(dias[4]); chkSabado.setSelected(dias[5]);
        chkDomingo.setSelected(dias[6]);

        String[] horas = dao.cargarHorasHorario(sel.getIdTecnico());
        if (horas[0].contains(":")) { txtInicioHH.setText(horas[0].split(":")[0]); txtInicioMM.setText(horas[0].split(":")[1]); }
        if (horas[1].contains(":")) { txtFinHH.setText(horas[1].split(":")[0]);    txtFinMM.setText(horas[1].split(":")[1]); }
    }

    // ── Guardar ──────────────────────────────────────────────
    @FXML public void guardarTecnico() {
        String error = validarCompleto();
        if (error != null) { mostrarMensaje(error, false); return; }

        Tecnico t = new Tecnico();
        t.setDni(txtDni.getText().trim());
        t.setNombres(txtNombres.getText().trim());
        t.setApellidos(txtApellidos.getText().trim());
        t.setTelefono(txtTelefono.getText().trim());
        t.setEmail(txtEmail.getText().trim());
        t.setIdEspecialidad(cmbEspecialidad.getValue().getIdEspecialidad());
        t.setIdZona(cmbDistrito.getValue() != null ? cmbDistrito.getValue().getIdZona() : 0);

        t.setMaxSolicitudesDia(spMaxAsignaciones.getValue());
        t.setObservaciones("");
        t.setEstado(tecnicoSeleccionado == null ? 1 : tecnicoSeleccionado.getEstado());

        boolean ok;
        int idFinal;
        if (tecnicoSeleccionado == null) {
            if (dao.existeDni(t.getDni(), 0)) { mostrarMensaje("El DNI ya está registrado.", false); return; }
            ok = dao.guardar(t);
            idFinal = ok ? obtenerIdPorDni(t.getDni()) : -1;
        } else {
            if (dao.existeDni(t.getDni(), tecnicoSeleccionado.getIdTecnico())) { mostrarMensaje("El DNI ya está registrado.", false); return; }
            t.setIdTecnico(tecnicoSeleccionado.getIdTecnico());
            ok = dao.actualizar(t);
            idFinal = tecnicoSeleccionado.getIdTecnico();
        }

        if (ok) {
            if (idFinal > 0) {
                String horaInicio = obtenerHora(txtInicioHH, txtInicioMM, "08:00");
                String horaFin    = obtenerHora(txtFinHH,    txtFinMM,    "17:00");
                boolean[] dias = {
                    chkLunes.isSelected(), chkMartes.isSelected(), chkMiercoles.isSelected(),
                    chkJueves.isSelected(), chkViernes.isSelected(),
                    chkSabado.isSelected(), chkDomingo.isSelected()
                };
                dao.guardarHorario(idFinal, dias, horaInicio, horaFin);
            }
            // Toast de éxito
            mostrarToast(tecnicoSeleccionado == null ? "✔  Técnico nuevo registrado" : "✔  Técnico actualizado", true);
            mostrarMensaje(tecnicoSeleccionado == null ? "Técnico registrado." : "Técnico actualizado.", true);
            cargarTabla(); limpiarFormulario();
        } else {
            mostrarMensaje("Error al guardar.", false);
        }
    }

    // ── Eliminar / Activar ───────────────────────────────────
    @FXML public void eliminarTecnico() {
        if (tecnicoSeleccionado == null) return;
        boolean activo = tecnicoSeleccionado.getEstado() == 1;
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Deseas " + (activo ? "desactivar" : "activar")
            + " a " + tecnicoSeleccionado.getNombreCompleto() + "?",
            ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                tecnicoSeleccionado.setEstado(activo ? 0 : 1);
                if (dao.actualizar(tecnicoSeleccionado)) {
                    mostrarToast("✔  Técnico " + (activo ? "desactivado" : "activado"), true);
                    mostrarMensaje("Técnico " + (activo ? "desactivado" : "activado") + ".", true);
                    cargarTabla(); limpiarFormulario();
                }
            }
        });
    }

    // ── Limpiar ──────────────────────────────────────────────
    @FXML public void limpiarFormulario() {
        tecnicoSeleccionado = null;
        txtDni.clear(); txtNombres.clear(); txtApellidos.clear();
        txtTelefono.clear(); txtEmail.clear();
        cmbEspecialidad.setValue(null); cmbDistrito.setValue(null);
        spMaxAsignaciones.getValueFactory().setValue(6);
        txtInicioHH.setText("08"); txtInicioMM.setText("00");
        txtFinHH.setText("17");   txtFinMM.setText("00");
        chkLunes.setSelected(true); chkMartes.setSelected(true);
        chkMiercoles.setSelected(true); chkJueves.setSelected(true);
        chkViernes.setSelected(true); chkSabado.setSelected(false); chkDomingo.setSelected(false);
        lblMensaje.setText(""); lblTituloForm.setText("Nuevo técnico");
        btnEliminar.setDisable(true); actualizarBoton(true); btnGuardar.setDisable(true);
        tblTecnicos.getSelectionModel().clearSelection();
    }

    // ── Privados ─────────────────────────────────────────────
    private void actualizarBoton(boolean activo) {
        if (activo) {
            btnEliminar.setText("Desactivar");
            btnEliminar.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20 9 20; -fx-background-radius: 6; -fx-cursor: hand;");
        } else {
            btnEliminar.setText("Activar");
            btnEliminar.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 9 20 9 20; -fx-background-radius: 6; -fx-cursor: hand;");
        }
    }

    private String validarCompleto() {
        String dni = txtDni.getText().trim();
        if (dni.isEmpty())           return "El DNI es obligatorio.";
        if (!dni.matches("\\d{8}"))  return "El DNI debe tener exactamente 8 dígitos.";
        if (txtNombres.getText().trim().isEmpty())   return "Los nombres son obligatorios.";
        if (txtApellidos.getText().trim().isEmpty()) return "Los apellidos son obligatorios.";
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            return "El correo no tiene formato válido.";
        if (cmbEspecialidad.getValue() == null) return "Selecciona la especialidad.";
        return null;
    }

    private void mostrarMensaje(String t, boolean ok) {
        lblMensaje.setText(t);
        lblMensaje.setStyle(ok ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
                               : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
    }
    private String nvl(String s) { return s == null ? "" : s; }

    private int obtenerIdPorDni(String dni) {
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion()
                .prepareStatement("SELECT id_tecnico FROM tb_tecnico WHERE dni=?")) {
            ps.setString(1, dni);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("[TecnicoController] obtenerIdPorDni: " + e.getMessage()); }
        return -1;
    }

    private String obtenerHora(TextField tfHH, TextField tfMM, String fallback) {
        String hh = tfHH.getText().trim(), mm = tfMM.getText().trim();
        if (hh.isEmpty() || mm.isEmpty()) return fallback;
        int h = Math.max(8, Math.min(19, Integer.parseInt(hh)));
        int m = Math.max(0, Math.min(59, Integer.parseInt(mm)));
        if (h == 19) m = 0;
        return String.format("%02d:%02d", h, m);
    }

    /**
     * Toast animado (fade-in → pausa 2.5 s → fade-out).
     * Requiere un StackPane con fx:id="toastPane" en el FXML raíz.
     */
    private void mostrarToast(String mensaje, boolean exito) {
        if (toastPane == null) return;

        // Label con padding CSS — se auto-dimensiona solo al texto
        Label toast = new Label(mensaje);
        toast.setWrapText(false);
        toast.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 18;" +
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