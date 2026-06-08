package Controlador;

import Conexion.ConexionDB;
import Modelo.Usuario;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.*;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.sql.*;
import java.time.LocalDate;

public class AdminReportesController implements ControladorConUsuario {

    @FXML private ComboBox<String> cmbTipoReporte;
    @FXML private HBox             panelFiltroEstado;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private HBox             panelFiltroFecha;
    @FXML private DatePicker       dpDesde;
    @FXML private DatePicker       dpHasta;

    @FXML private Label kpi1Valor, kpi1Label;
    @FXML private Label kpi2Valor, kpi2Label;
    @FXML private Label kpi3Valor, kpi3Label;
    @FXML private Label kpi4Valor, kpi4Label;

    @FXML private TableView<ObservableList<String>> tblReporte;
    @FXML private Label lblTituloTabla;
    @FXML private Label lblContador;

    private Connection getConn() { return ConexionDB.getInstancia().getConexion(); }

    // ─── Inicialización ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        cmbTipoReporte.setItems(FXCollections.observableArrayList(
            "Reporte de Clientes",
            "Reporte de Técnicos",
            "Reporte de Solicitudes"
        ));
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "Todos", "Activo", "Inactivo"
        ));
        cmbFiltroEstado.setValue("Todos");
        dpDesde.setValue(LocalDate.of(2000, 1, 1));
        dpHasta.setValue(LocalDate.now());

        // Listeners: actualiza tabla automáticamente al cambiar filtros
        cmbTipoReporte.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                actualizarPanelFiltros(val);
                cargarReporte();
            }
        });
        cmbFiltroEstado.valueProperty().addListener((obs, old, val) -> cargarReporte());
        dpDesde.valueProperty().addListener((obs, old, val) -> cargarReporte());
        dpHasta.valueProperty().addListener((obs, old, val) -> cargarReporte());

        // Carga inicial
        cmbTipoReporte.setValue("Reporte de Clientes");
        actualizarPanelFiltros("Reporte de Clientes");
        cargarReporte();
    }

    @Override public void setUsuarioActual(Usuario usuario) {}

    // ─── onAction del ComboBox tipo (por si se usa en FXML también) ───────────
    @FXML
    public void onTipoReporteChanged() {
        String tipo = cmbTipoReporte.getValue();
        if (tipo == null) return;
        actualizarPanelFiltros(tipo);
        cargarReporte();
    }

    private void actualizarPanelFiltros(String tipo) {
        boolean esSolicitud = "Reporte de Solicitudes".equals(tipo);
        panelFiltroFecha.setVisible(esSolicitud);
        panelFiltroFecha.setManaged(esSolicitud);
        panelFiltroEstado.setVisible(!esSolicitud);
        panelFiltroEstado.setManaged(!esSolicitud);
    }

    // Carga la tabla (llamado por listeners automáticos)
    private void cargarReporte() {
        String tipo = cmbTipoReporte.getValue();
        if (tipo == null) return;
        tblReporte.getColumns().clear();
        tblReporte.getItems().clear();
        switch (tipo) {
            case "Reporte de Clientes"    -> reporteClientes();
            case "Reporte de Técnicos"    -> reporteTecnicos();
            case "Reporte de Solicitudes" -> reporteSolicitudes();
        }
    }

    // Botón "Generar Reporte": carga tabla y exporta PDF
    @FXML
    public void generarReporte() {
        cargarReporte();
        exportarPDF();
    }

    @FXML
    public void limpiar() {
        cmbFiltroEstado.setValue("Todos");
        dpDesde.setValue(LocalDate.of(2000, 1, 1));
        dpHasta.setValue(LocalDate.now());
        // Los listeners disparan generarReporte() automáticamente
    }

    // ─── Reporte Clientes ─────────────────────────────────────────────────────
    // Columnas: ID · DNI · Nombre · Teléfono · Email · Distrito · Estado · Solicitudes · Registro
    // (sin Zona — Distrito ya la incluye)

    private void reporteClientes() {
        lblTituloTabla.setText("Clientes registrados");
        String where = buildFiltroEstado("c.estado");
        String sql =
            "SELECT c.id_cliente, " +
            "       c.dni, " +
            "       CONCAT(c.nombres,' ',c.apellidos) AS nombre_completo, " +
            "       COALESCE(c.telefono,'—') AS telefono, " +
            "       COALESCE(c.email,'—') AS email, " +
            "       COALESCE(z.nombre, c.distrito,'—') AS distrito, " +
            "       IF(c.estado=1,'Activo','Inactivo') AS estado, " +
            "       COUNT(s.id_solicitud) AS solicitudes, " +
            "       DATE_FORMAT(c.fecha_registro,'%d/%m/%Y') AS registro " +
            "FROM tb_cliente c " +
            "LEFT JOIN tb_zona z ON c.id_zona = z.id_zona " +
            "LEFT JOIN tb_solicitud s ON s.id_cliente = c.id_cliente " +
            where +
            "GROUP BY c.id_cliente " +
            "ORDER BY c.fecha_registro DESC";

        String[] cols = {"ID","DNI","Nombre completo","Teléfono","Email",
                         "Distrito","Estado","Solicitudes","Registro"};
        cargarTabla(sql, cols);

        kpiQuery("SELECT COUNT(*) FROM tb_cliente",                kpi1Valor, kpi1Label, "Total clientes");
        kpiQuery("SELECT COUNT(*) FROM tb_cliente WHERE estado=1", kpi2Valor, kpi2Label, "Activos");
        kpiQuery("SELECT COUNT(*) FROM tb_cliente WHERE estado=0", kpi3Valor, kpi3Label, "Inactivos");
        kpiQuery("SELECT COUNT(DISTINCT id_zona) FROM tb_cliente", kpi4Valor, kpi4Label, "Zonas atendidas");
    }

    // ─── Reporte Técnicos ─────────────────────────────────────────────────────
    // Columnas: ID · Técnico · Especialidad · Distrito · Disponibilidad · Estado · Asignaciones
    // (sin Nivel, sin Eficiencia, sin Completadas; Zona renombrado a Distrito)

    private void reporteTecnicos() {
        lblTituloTabla.setText("Técnicos registrados");
        String where = buildFiltroEstado("t.estado");
        String sql =
            "SELECT t.id_tecnico, " +
            "       CONCAT(t.nombres,' ',t.apellidos) AS tecnico, " +
            "       COALESCE(e.nombre,'—') AS especialidad, " +
            "       t.dni, " +
            "       COALESCE(z.nombre,'—') AS distrito, " +
            "       IF(t.estado=1,'Activo','Inactivo') AS estado, " +
            "       COUNT(a.id_asignacion) AS asignaciones " +
            "FROM tb_tecnico t " +
            "LEFT JOIN tb_especialidad e ON t.id_especialidad = e.id_especialidad " +
            "LEFT JOIN tb_zona z ON t.id_zona = z.id_zona " +
            "LEFT JOIN tb_asignacion a ON a.id_tecnico = t.id_tecnico " +
            where +
            "GROUP BY t.id_tecnico " +
            "ORDER BY t.nombres ASC";

        String[] cols = {"ID","Técnico","Especialidad","DNI","Distrito",
                         "Estado","Asignaciones"};
        cargarTabla(sql, cols);

        kpiQuery("SELECT COUNT(*) FROM tb_tecnico",                kpi1Valor, kpi1Label, "Total técnicos");
        kpiQuery("SELECT COUNT(*) FROM tb_tecnico WHERE estado=1", kpi2Valor, kpi2Label, "Activos");
        kpiQuery("SELECT COUNT(*) FROM tb_tecnico WHERE estado=0", kpi3Valor, kpi3Label, "Inactivos");
        kpiQuery("SELECT COUNT(DISTINCT id_especialidad) FROM tb_tecnico WHERE estado=1",
                 kpi4Valor, kpi4Label, "Especialidades");
    }

    // ─── Reporte Solicitudes ──────────────────────────────────────────────────
    // Columnas: ID · Fecha · Hora · Cliente · Tipo Servicio · Estado · Prioridad · Distrito · Técnico
    // (sin Código, sin Estado Asig.; Zona→Distrito; Fecha dividida en Fecha y Hora)

    private void reporteSolicitudes() {
        lblTituloTabla.setText("Solicitudes del período");
        LocalDate desde = dpDesde.getValue() != null ? dpDesde.getValue() : LocalDate.of(2000, 1, 1);
        LocalDate hasta = dpHasta.getValue() != null ? dpHasta.getValue() : LocalDate.now();
        String d = desde + " 00:00:00";
        String h = hasta + " 23:59:59";

        String sql =
            "SELECT s.id_solicitud, " +
            "       DATE_FORMAT(s.fecha_solicitada,'%d/%m/%Y') AS fecha, " +
            "       COALESCE(s.horario_preferido,'—') AS hora, " +
            "       CONCAT(c.nombres,' ',c.apellidos) AS cliente, " +
            "       ts.nombre AS tipo_servicio, " +
            "       s.estado, " +
            "       s.prioridad, " +
            "       COALESCE(z.nombre,'—') AS distrito, " +
            "       COALESCE(CONCAT(t.nombres,' ',t.apellidos),'Sin asignar') AS tecnico " +
            "FROM tb_solicitud s " +
            "JOIN tb_cliente c ON s.id_cliente = c.id_cliente " +
            "JOIN tb_tipo_servicio ts ON s.id_tipo_servicio = ts.id_tipo_servicio " +
            "LEFT JOIN tb_zona z ON c.id_zona = z.id_zona " +
            "LEFT JOIN tb_asignacion a ON a.id_solicitud = s.id_solicitud " +
            "LEFT JOIN tb_tecnico t ON a.id_tecnico = t.id_tecnico " +
            "WHERE s.fecha_registro BETWEEN ? AND ? " +
            "ORDER BY s.fecha_solicitada DESC, s.horario_preferido DESC";

        String[] cols = {"ID","Fecha","Hora","Cliente","Tipo Servicio",
                         "Estado","Prioridad","Distrito","Técnico"};
        cargarTablaConParams(sql, cols, d, h);

        kpiQueryConParams("SELECT COUNT(*) FROM tb_solicitud WHERE fecha_registro BETWEEN ? AND ?",
                          kpi1Valor, kpi1Label, "Total solicitudes", d, h);
        kpiQueryConParams("SELECT COUNT(*) FROM tb_solicitud WHERE estado='PENDIENTE' AND fecha_registro BETWEEN ? AND ?",
                          kpi2Valor, kpi2Label, "Pendientes", d, h);
        kpiQueryConParams("SELECT COUNT(*) FROM tb_solicitud WHERE estado IN ('COMPLETADA','CERRADA') AND fecha_registro BETWEEN ? AND ?",
                          kpi3Valor, kpi3Label, "Completadas", d, h);
        kpiQueryConParams("SELECT COUNT(*) FROM tb_solicitud WHERE estado='CANCELADA' AND fecha_registro BETWEEN ? AND ?",
                          kpi4Valor, kpi4Label, "Canceladas", d, h);
    }

    // ─── Internos ─────────────────────────────────────────────────────────────

    private void cargarTabla(String sql, String[] cabeceras) {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            buildColumns(cabeceras, rs.getMetaData().getColumnCount());
            while (rs.next()) datos.add(filaDesde(rs, rs.getMetaData().getColumnCount()));
            tblReporte.setItems(datos);
            lblContador.setText(datos.size() + " registros");
        } catch (SQLException e) {
            System.err.println("[AdminReportesController.cargarTabla] " + e.getMessage());
            lblContador.setText("Error: " + e.getMessage());
        }
    }

    private void cargarTablaConParams(String sql, String[] cabeceras, String desde, String hasta) {
        ObservableList<ObservableList<String>> datos = FXCollections.observableArrayList();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            buildColumns(cabeceras, rs.getMetaData().getColumnCount());
            while (rs.next()) datos.add(filaDesde(rs, rs.getMetaData().getColumnCount()));
            tblReporte.setItems(datos);
            lblContador.setText(datos.size() + " registros");
        } catch (SQLException e) {
            System.err.println("[AdminReportesController.cargarTabla] " + e.getMessage());
            lblContador.setText("Error: " + e.getMessage());
        }
    }

    private void buildColumns(String[] cabeceras, int numCols) {
        tblReporte.getColumns().clear();
        for (int i = 0; i < Math.min(cabeceras.length, numCols); i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(cabeceras[i]);
            col.setCellValueFactory(p -> new SimpleStringProperty(
                idx < p.getValue().size() ? p.getValue().get(idx) : ""));

            // Anchos personalizados
            double width;
            switch (cabeceras[i]) {
                case "ID"              -> width = 50;
                case "Email"           -> width = 190;
                case "Distrito"        -> width = 160;
                case "Nombre completo",
                     "Técnico",
                     "Cliente"         -> width = 180;
                case "Tipo Servicio"   -> width = 150;
                case "Especialidad"    -> width = 140;
                case "Estado",
                     "Solicitudes",
                     "Asignaciones",
                     "Hora"            -> width = 100;
                case "Prioridad",
                     "Registro",
                     "Fecha"           -> width = 110;
                default                -> width = 120;
            }
            col.setPrefWidth(width);

            col.setStyle("-fx-alignment: CENTER;");

            tblReporte.getColumns().add(col);
        }
    }

    private ObservableList<String> filaDesde(ResultSet rs, int numCols) throws SQLException {
        ObservableList<String> fila = FXCollections.observableArrayList();
        for (int i = 1; i <= numCols; i++) {
            String v = rs.getString(i);
            fila.add(v != null ? v : "");
        }
        return fila;
    }

    private void kpiQuery(String sql, Label lblValor, Label lblTexto, String texto) {
        lblTexto.setText(texto);
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            lblValor.setText(rs.next() ? rs.getString(1) : "0");
        } catch (SQLException e) { lblValor.setText("—"); }
    }

    private void kpiQueryConParams(String sql, Label lblValor, Label lblTexto,
                                    String texto, String desde, String hasta) {
        lblTexto.setText(texto);
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, desde);
            ps.setString(2, hasta);
            ResultSet rs = ps.executeQuery();
            lblValor.setText(rs.next() ? rs.getString(1) : "0");
        } catch (SQLException e) { lblValor.setText("—"); }
    }

    private String buildFiltroEstado(String campo) {
        String val = cmbFiltroEstado.getValue();
        if (val == null || "Todos".equals(val)) return "WHERE 1=1 ";
        return "WHERE " + campo + " = " + ("Activo".equals(val) ? 1 : 0) + " ";
    }

    // ─── Exportar PDF ─────────────────────────────────────────────────────────

    @FXML
    public void exportarPDF() {
        if (tblReporte.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                "No hay datos para exportar. Genera un reporte primero.")
                .showAndWait();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte PDF");
        fc.setInitialFileName(lblTituloTabla.getText().replace(" ", "_") + ".pdf");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivo PDF", "*.pdf"));
        File archivo = fc.showSaveDialog(tblReporte.getScene().getWindow());
        if (archivo == null) return;

        try {
            PdfWriter writer = new PdfWriter(archivo.getAbsolutePath());
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf,
                com.itextpdf.kernel.geom.PageSize.A4.rotate());
            doc.setMargins(30, 30, 30, 30);

            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Título
            doc.add(new Paragraph("Sistema de Asignación de Servicios")
                .setFont(fontBold).setFontSize(14)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph(lblTituloTabla.getText())
                .setFont(fontBold).setFontSize(11)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(0x15, 0x65, 0xC0))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

            doc.add(new Paragraph(
                "Generado: " +
                java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                "    |    " + lblContador.getText())
                .setFont(fontNormal).setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12));

            // KPIs
            Table kpiTable = new Table(4).useAllAvailableWidth().setMarginBottom(14);
            addKpiCell(kpiTable, kpi1Label.getText(), kpi1Valor.getText(), fontBold, fontNormal);
            addKpiCell(kpiTable, kpi2Label.getText(), kpi2Valor.getText(), fontBold, fontNormal);
            addKpiCell(kpiTable, kpi3Label.getText(), kpi3Valor.getText(), fontBold, fontNormal);
            addKpiCell(kpiTable, kpi4Label.getText(), kpi4Valor.getText(), fontBold, fontNormal);
            doc.add(kpiTable);

            // Tabla de datos
            int numCols = tblReporte.getColumns().size();
            Table tabla = new Table(numCols).useAllAvailableWidth();

            // Encabezados
            for (TableColumn<?, ?> col : tblReporte.getColumns()) {
                tabla.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(col.getText()).setFont(fontBold).setFontSize(8))
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(0x15, 0x65, 0xC0))
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5));
            }

            // Filas
            boolean par = false;
            for (ObservableList<String> fila : tblReporte.getItems()) {
                com.itextpdf.kernel.colors.Color bg = par
                    ? new com.itextpdf.kernel.colors.DeviceRgb(0xF0, 0xF4, 0xF8)
                    : ColorConstants.WHITE;
                for (int i = 0; i < numCols; i++) {
                    String val = i < fila.size() ? fila.get(i) : "";
                    tabla.addCell(new com.itextpdf.layout.element.Cell()
                        .add(new Paragraph(val).setFont(fontNormal).setFontSize(8))
                        .setBackgroundColor(bg)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(4));
                }
                par = !par;
            }
            doc.add(tabla);
            doc.close();

            // Diálogo de éxito personalizado
            javafx.scene.control.Dialog<Void> dlg = new javafx.scene.control.Dialog<>();
            dlg.setTitle("Reporte generado");
            dlg.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.OK);

            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.setPadding(new javafx.geometry.Insets(20, 28, 10, 28));
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.control.Label icono = new javafx.scene.control.Label("✔");
            icono.setStyle("-fx-font-size: 32px; -fx-text-fill: #2E7D32;");

            javafx.scene.control.Label titulo = new javafx.scene.control.Label("PDF generado correctamente");
            titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

            javafx.scene.control.Label ruta = new javafx.scene.control.Label(archivo.getAbsolutePath());
            ruta.setStyle("-fx-font-size: 11px; -fx-text-fill: #5A6A7A; -fx-wrap-text: true;");
            ruta.setMaxWidth(380);
            ruta.setWrapText(true);

            javafx.scene.control.Label reporte = new javafx.scene.control.Label(
                "Reporte: " + lblTituloTabla.getText() + "  ·  " + lblContador.getText());
            reporte.setStyle("-fx-font-size: 11px; -fx-text-fill: #1565C0;");

            javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(14);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            header.getChildren().addAll(icono, new javafx.scene.layout.VBox(4, titulo, reporte));

            content.getChildren().addAll(header,
                new javafx.scene.control.Separator(), ruta);

            dlg.getDialogPane().setContent(content);
            dlg.getDialogPane().setStyle("-fx-background-color: white;");
            dlg.showAndWait();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                "Error al generar PDF:\n" + e.getMessage())
                .showAndWait();
            e.printStackTrace();
        }
    }

    private void addKpiCell(Table t, String label, String valor,
                             PdfFont fontBold, PdfFont fontNormal) {
        com.itextpdf.layout.element.Cell c = new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(valor)
                .setFont(fontBold).setFontSize(18)
                .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(0x15, 0x65, 0xC0))
                .setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph(label)
                .setFont(fontNormal).setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER))
            .setBorder(new com.itextpdf.layout.borders.SolidBorder(
                new com.itextpdf.kernel.colors.DeviceRgb(0xDD, 0xE3, 0xEA), 1))
            .setPadding(8);
        t.addCell(c);
    }
}
