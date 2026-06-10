package Controlador;

import Conexion.ConexionDB;
import Modelo.Solicitud;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuPrincipalController {

    @FXML private Label lblModuloActual;
    @FXML private Label lblFechaHora;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblNombreUsuario;
    @FXML private Label kpiClientes;
    @FXML private Label kpiSolicitudes;
    @FXML private Label kpiTecnicos;
    @FXML private Label kpiAsignaciones;

    // Columnas de la tabla del dashboard
    @FXML private TableView<Solicitud>             tblUltimasSolicitudes;
    @FXML private TableColumn<Solicitud, Integer>  colIdSol;
    @FXML private TableColumn<Solicitud, String>   colClienteSol;
    @FXML private TableColumn<Solicitud, String>   colFechaSol;
    @FXML private TableColumn<Solicitud, String>   colHoraSol;
    @FXML private TableColumn<Solicitud, String>   colDireccionSol;
    @FXML private TableColumn<Solicitud, String>   colTipoSol;
    @FXML private TableColumn<Solicitud, String>   colTecnicoSol;
    @FXML private TableColumn<Solicitud, String>   colEstadoSol;
    @FXML private TableColumn<Solicitud, String>   colPrioridadSol;

    @FXML private StackPane contentArea;
    @FXML private Button btnDashboard;
    @FXML private Button btnClientes;
    @FXML private Button btnSolicitudes;
    @FXML private Button btnTecnicos;
    @FXML private Button btnAsignacionAuto;
    @FXML private Button btnGestionAsignaciones;
    @FXML private Button btnHistorial;
    @FXML private Button btnReportes;

    private Node dashboardOriginal;

    @FXML
    public void initialize() {
        // Fecha en topbar
        String fecha = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("es","PE")));
        lblFechaHora.setText(capitalize(fecha));

        // Guardar nodo dashboard
        if (!contentArea.getChildren().isEmpty()) {
            dashboardOriginal = contentArea.getChildren().get(0);
        }

        // Configurar columnas de la tabla
        colIdSol.setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colClienteSol.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFechaSol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getFechaSolicitada() != null
                ? d.getValue().getFechaSolicitada().toString() : ""));
        colHoraSol.setCellValueFactory(new PropertyValueFactory<>("horarioPreferido"));
        colDireccionSol.setCellValueFactory(d -> new SimpleStringProperty(
            nvl(d.getValue().getDireccionCliente())));
        colTipoSol.setCellValueFactory(new PropertyValueFactory<>("nombreTipo"));
        colTecnicoSol.setCellValueFactory(d -> new SimpleStringProperty(
            nvl(d.getValue().getNombreTecnico())));
        colEstadoSol.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colPrioridadSol.setCellValueFactory(new PropertyValueFactory<>("prioridad"));

        // Colorear estado igual que en AsignacionView
        colEstadoSol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item.toUpperCase()) {
                    case "PENDIENTE"  -> "#F57F17";
                    case "ASIGNADA"   -> "#1565C0";
                    case "COMPLETADA" -> "#2E7D32";
                    case "CANCELADA"  -> "#C62828";
                    default           -> "#5A6A7A";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        cargarDashboard();
    }

    // ── Dashboard ────────────────────────────────────────────

    private void cargarDashboard() {
        cargarKPIs();
        cargarUltimasSolicitudes();
    }

    private void cargarKPIs() {
        Connection conn = ConexionDB.getInstancia().getConexion();

        // Clientes registrados
        setKpi(kpiClientes, conn,
            "SELECT COUNT(*) FROM tb_cliente WHERE estado=1");

        // Solicitudes pendientes
        setKpi(kpiSolicitudes, conn,
            "SELECT COUNT(*) FROM tb_solicitud WHERE estado='PENDIENTE'");

        // Técnicos activos (total)
        setKpi(kpiTecnicos, conn,
            "SELECT COUNT(*) FROM tb_tecnico WHERE estado=1");

        // Solicitudes completadas / total solicitudes
        int completadas = queryInt(conn,
            "SELECT COUNT(*) FROM tb_solicitud WHERE estado='COMPLETADA'");
        int total = queryInt(conn,
            "SELECT COUNT(*) FROM tb_solicitud");
        if (kpiAsignaciones != null)
            kpiAsignaciones.setText(completadas + " / " + total);
    }

    private void setKpi(Label lbl, Connection conn, String sql) {
        if (lbl == null) return;
        lbl.setText(String.valueOf(queryInt(conn, sql)));
    }

    private int queryInt(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[MenuPrincipal] queryInt: " + e.getMessage());
        }
        return 0;
    }

    private void cargarUltimasSolicitudes() {
        if (tblUltimasSolicitudes == null) return;
        List<Solicitud> lista = new ArrayList<>();
        String sql =
            "SELECT s.id_solicitud, " +
            "       CONCAT(c.nombres,' ',c.apellidos) AS nombre_cliente, " +
            "       c.direccion AS direccion_cliente, " +
            "       s.fecha_solicitada, s.horario_preferido, " +
            "       ts.nombre AS nombre_tipo, " +
            "       s.prioridad, s.estado, " +
            "       CONCAT(IFNULL(t.nombres,''),' ',IFNULL(t.apellidos,'')) AS nombre_tecnico " +
            "FROM tb_solicitud s " +
            "JOIN tb_cliente c ON c.id_cliente = s.id_cliente " +
            "JOIN tb_tipo_servicio ts ON ts.id_tipo_servicio = s.id_tipo_servicio " +
            "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud " +
            "LEFT JOIN tb_tecnico t ON t.id_tecnico = a.id_tecnico " +
            "ORDER BY s.id_solicitud DESC " +
            "LIMIT 15";
        try (PreparedStatement ps = ConexionDB.getInstancia().getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Solicitud s = new Solicitud();
                s.setIdSolicitud(rs.getInt("id_solicitud"));
                s.setNombreCliente(rs.getString("nombre_cliente"));
                s.setDireccionCliente(rs.getString("direccion_cliente"));
                java.sql.Date fd = rs.getDate("fecha_solicitada");
                if (fd != null) s.setFechaSolicitada(fd.toLocalDate());
                s.setHorarioPreferido(rs.getString("horario_preferido"));
                s.setNombreTipo(rs.getString("nombre_tipo"));
                s.setPrioridad(rs.getString("prioridad"));
                s.setEstado(rs.getString("estado"));
                s.setNombreTecnico(rs.getString("nombre_tecnico"));
                lista.add(s);
            }
        } catch (SQLException e) {
            System.err.println("[MenuPrincipal] cargarUltimasSolicitudes: " + e.getMessage());
        }
        tblUltimasSolicitudes.setItems(FXCollections.observableArrayList(lista));
    }

    // ── Navegación ───────────────────────────────────────────

    @FXML public void mostrarDashboard() {
        marcarActivo(btnDashboard);
        lblModuloActual.setText("Panel principal");
        if (dashboardOriginal != null) {
            contentArea.getChildren().setAll(dashboardOriginal);
        }
        cargarDashboard(); // refrescar datos cada vez que se vuelve
    }

    @FXML public void mostrarClientes() {
        marcarActivo(btnClientes);
        lblModuloActual.setText("Gestión de clientes");
        cargarVista("ClienteView.fxml");
    }

    @FXML public void mostrarSolicitudes() {
        marcarActivo(btnSolicitudes);
        lblModuloActual.setText("Gestión de solicitudes");
        cargarVista("SolicitudView.fxml");
    }

    @FXML public void mostrarTecnicos() {
        marcarActivo(btnTecnicos);
        lblModuloActual.setText("Gestión de técnicos");
        cargarVista("TecnicoView.fxml");
    }

    @FXML public void mostrarAsignacionAuto() {
        marcarActivo(btnAsignacionAuto);
        lblModuloActual.setText("Asignación automática");
        cargarVista("AsignacionView.fxml");
    }

    @FXML public void mostrarGestionAsignaciones() {
        marcarActivo(btnGestionAsignaciones);
        lblModuloActual.setText("Gestión de asignaciones");
        cargarVista("AsignacionView.fxml");
    }

    @FXML public void mostrarHistorial() {
        marcarActivo(btnHistorial);
        lblModuloActual.setText("Historial y seguimiento");
        cargarVista("HistorialView.fxml");
    }

    @FXML public void mostrarReportes() {
        marcarActivo(btnReportes);
        lblModuloActual.setText("Reportes");
        cargarVista("ReporteView.fxml");
    }

    @FXML public void cerrarSesion() {
        try {
            URL url = getClass().getResource("../Vista/LoginView.fxml");
            if (url == null) url = getClass().getResource("/Vista/LoginView.fxml");
            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Privados ─────────────────────────────────────────────

    private void cargarVista(String nombreArchivo) {
        try {
            URL url = getClass().getResource("../Vista/" + nombreArchivo);
            if (url == null) url = getClass().getResource("/Vista/" + nombreArchivo);
            if (url == null) { mostrarError("No se encontró: " + nombreArchivo); return; }
            FXMLLoader loader = new FXMLLoader(url);
            Node vista = loader.load();
            contentArea.getChildren().setAll(vista);
        } catch (Exception e) {
            mostrarError("Error al cargar " + nombreArchivo + ":\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        Label lbl = new Label(mensaje);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #C62828;");
        lbl.setWrapText(true);
        contentArea.getChildren().setAll(lbl);
    }

    private void marcarActivo(Button activo) {
        Button[] todos = {btnDashboard, btnClientes, btnSolicitudes, btnTecnicos,
                          btnAsignacionAuto, btnGestionAsignaciones,
                          btnHistorial, btnReportes};
        for (Button b : todos) if (b != null) b.getStyleClass().setAll("sidebar-item");
        if (activo != null) activo.getStyleClass().setAll("sidebar-item-active");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private String nvl(String s) { return (s == null || s.isBlank()) ? "—" : s.trim(); }
}
