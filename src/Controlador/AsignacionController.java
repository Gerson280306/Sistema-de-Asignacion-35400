package Controlador;

import Util.Log;

import DAO.AsignacionDAO;
import DAO.SolicitudDAO;
import DAO.TecnicoDAO;
import Modelo.Asignacion;
import Modelo.Solicitud;
import Modelo.Tecnico;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controlador unificado: Asignación automática + Gestión de asignaciones.
 *
 * Tabla única que muestra todas las solicitudes sin importar su estado.
 * El panel derecho permite asignar técnico solo a las solicitudes PENDIENTE.
 *
 * ALGORITMO DE ASIGNACIÓN:
 *  1. Las solicitudes se ordenan por prioridad: CRITICA > ALTA > MEDIA > BAJA
 *  2. Solo técnicos activos cuyo horario cubra la hora de la solicitud y trabajen ese día.
 *  3. Un técnico queda bloqueado ±3 horas alrededor de cada asignación (2 h trabajo + 1 h traslado).
 *  4. Puntaje de selección: +2 especialidad coincide, +1 zona coincide → gana el mayor.
 */
public class AsignacionController {

    private final SolicitudDAO  solicitudDAO  = new SolicitudDAO();
    private final TecnicoDAO    tecnicoDAO    = new TecnicoDAO();
    /** StackPane raíz del FXML — necesario para el toast */
    @FXML private StackPane toastPane;

    private final AsignacionDAO asignacionDAO = new AsignacionDAO();

    // ── Encabezado ───────────────────────────────────────────
    @FXML private Label lblPendientes, lblAsignadas, lblTecnicosLibres;

    // ── Barra de búsqueda ─────────────────────────────────────
    @FXML private TextField  txtBuscar;
    @FXML private DatePicker dpFiltroFecha;

    // ── Tabla única ───────────────────────────────────────────
    @FXML private TableView<Solicitud>           tblSolicitudes;
    @FXML private TableColumn<Solicitud,Integer> colId;
    @FXML private TableColumn<Solicitud,String>  colFecha, colHora, colCliente,
                                                  colDireccion, colTipo, colTecnico,
                                                  colEstado, colPrioridad;
    @FXML private Label lblContador;

    // ── Panel asignar técnico ─────────────────────────────────
    @FXML private Label  lblSolicitudSeleccionada, lblDireccionSolicitud,
                          lblFechaSolicitud, lblHoraSolicitud,
                          lblTecnicoSugerido, lblRazonSugerencia;
    @FXML private ComboBox<Tecnico> cmbTecnicoManual;
    @FXML private TextArea          txtNotasAsignacion;
    @FXML private Button            btnAsignar;
    @FXML private Label             lblMensaje;

    // ── Estado interno ────────────────────────────────────────
    private int     idSolicitudSeleccionada  = -1;
    private ScheduledExecutorService scheduler;
    private int     idAsignacionSeleccionada = -1;
    private boolean esPendiente = false;
    private Tecnico tecnicoSugerido = null;

    // ── Prioridad numérica ────────────────────────────────────
    private static int pesoPrioridad(String p) {
        if (p == null) return 0;
        switch (p.toUpperCase()) {
            case "CRITICA": return 4;
            case "ALTA":    return 3;
            case "MEDIA":   return 2;
            case "BAJA":    return 1;
            default:        return 0;
        }
    }

    // ════════════════════════════════════════════════════════
    //  INICIALIZACIÓN
    // ════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        configurarColumnas();

        ObservableList<Tecnico> tecnicos = FXCollections.observableArrayList(tecnicoDAO.listarActivos());
        cmbTecnicoManual.setItems(tecnicos);

        cargarTodosLosData();
        limpiarPanelSeleccion();
        iniciarMonitorEstados();
    }

    /** Revisa cada 60 s si alguna solicitud debe cambiar de estado automáticamente. */
    private void iniciarMonitorEstados() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "monitor-estados");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime ahora = LocalDateTime.now();
                List<Solicitud> lista = solicitudDAO.listarParaAutoTerminar();
                boolean huboCambio = false;

                for (Solicitud s : lista) {
                    if (s.getFechaSolicitada() == null || s.getHorarioPreferido() == null) continue;
                    LocalTime hora = parsearHora(s.getHorarioPreferido());
                    if (hora == null) continue;

                    // DateTime exacto de la solicitud
                    LocalDateTime fechaHoraSolicitud = s.getFechaSolicitada().atTime(hora);
                    String estado = s.getEstado() != null ? s.getEstado().toUpperCase() : "";

                    // PENDIENTE sin asignar: 1 hora después → CANCELADA
                    if ("PENDIENTE".equals(estado)
                            && ahora.isAfter(fechaHoraSolicitud.plusHours(1))) {
                        solicitudDAO.cambiarEstado(s.getIdSolicitud(), "CANCELADA");
                        System.out.println("[Monitor] Solicitud #" + s.getIdSolicitud()
                                + " cancelada por timeout.");
                        huboCambio = true;

                    // ASIGNADA: 2 horas después → COMPLETADA
                    } else if ("ASIGNADA".equals(estado)
                            && ahora.isAfter(fechaHoraSolicitud.plusHours(2))) {
                        solicitudDAO.cambiarEstado(s.getIdSolicitud(), "COMPLETADA");
                        int idAsig = asignacionDAO.buscarIdPorSolicitud(s.getIdSolicitud());
                        if (idAsig > 0) {
                            asignacionDAO.cambiarEstado(idAsig, "COMPLETADA", -1,
                                    "Auto-completada por el sistema");
                        }
                        System.out.println("[Monitor] Solicitud #" + s.getIdSolicitud()
                                + " completada automaticamente.");
                        huboCambio = true;
                    }
                }
                if (huboCambio) {
                    Platform.runLater(() -> cargarTodosLosData());
                }
            } catch (Exception ex) {
                Log.warn("[Monitor] Error: " + ex.getMessage());
            }
        }, 5, 60, TimeUnit.SECONDS);
    }

    // ── Configuración de columnas ─────────────────────────────
    private void configurarColumnas() {
        colId       .setCellValueFactory(new PropertyValueFactory<>("idSolicitud"));
        colFecha    .setCellValueFactory(new PropertyValueFactory<>("fechaSolicitada"));
        colHora     .setCellValueFactory(new PropertyValueFactory<>("horarioPreferido"));
        colCliente  .setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colDireccion.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
            nvl(d.getValue().getDireccionCliente(), "")));
        colTipo     .setCellValueFactory(new PropertyValueFactory<>("nombreTipo"));
        colTecnico  .setCellValueFactory(new PropertyValueFactory<>("nombreTecnico"));
        colEstado   .setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columna estado con color
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setAlignment(javafx.geometry.Pos.CENTER);
                switch (item.toUpperCase()) {
                    case "PENDIENTE":  setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;"); break;
                    case "ASIGNADA":   setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;"); break;
                    case "EN_CAMINO":  setStyle("-fx-text-fill: #6A1B9A; -fx-font-weight: bold;"); break;
                    case "EN_PROCESO": setStyle("-fx-text-fill: #00695C; -fx-font-weight: bold;"); break;
                    case "COMPLETADA":  setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;"); break;
                    case "CANCELADA":   setStyle("-fx-text-fill: #757575;"); break;
                    default:           setStyle(""); break;
                }
            }
        });

        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        colPrioridad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setAlignment(javafx.geometry.Pos.CENTER);
                switch (item.toUpperCase()) {
                    case "CRITICA": setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;"); break;
                    case "ALTA":    setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;"); break;
                    case "MEDIA":   setStyle("-fx-text-fill: #1565C0;"); break;
                    default:        setStyle("-fx-text-fill: #37474F;"); break;
                }
            }
        });
    }

    // ════════════════════════════════════════════════════════
    //  CARGA DE DATOS
    // ════════════════════════════════════════════════════════
    private void cargarTodosLosData() {
        List<Solicitud> todas = solicitudDAO.listarTodos();

        // Ordenar: primero pendientes por prioridad, luego el resto
        todas.sort(Comparator
            .comparingInt((Solicitud s) -> {
                String e = s.getEstado() != null ? s.getEstado().toUpperCase() : "";
                // Pendientes primero, luego asignadas, luego el resto
                switch (e) {
                    case "PENDIENTE":  return 0;
                    case "ASIGNADA":   return 1;
                    case "EN_CAMINO":  return 2;
                    case "EN_PROCESO": return 3;
                    case "COMPLETADA": return 4;
                    case "CANCELADA":   return 5;
                    default:           return 5;
                }
            })
            .thenComparingInt((Solicitud s) -> -pesoPrioridad(s.getPrioridad()))
        );

        tblSolicitudes.setItems(FXCollections.observableArrayList(todas));

        long pendCount = todas.stream()
            .filter(s -> "PENDIENTE".equalsIgnoreCase(s.getEstado())).count();
        long asigCount = todas.stream()
            .filter(s -> {
                String e = s.getEstado() != null ? s.getEstado().toUpperCase() : "";
                return e.equals("ASIGNADA") || e.equals("EN_CAMINO") || e.equals("EN_PROCESO");
            }).count();

        actualizarContadores((int) pendCount, (int) asigCount, todas.size());
    }

    // ════════════════════════════════════════════════════════
    //  BÚSQUEDA con texto y/o fecha
    // ════════════════════════════════════════════════════════
    @FXML public void buscar() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        LocalDate fechaFiltro = (dpFiltroFecha != null) ? dpFiltroFecha.getValue() : null;

        if (texto.isEmpty() && fechaFiltro == null) { cargarTodosLosData(); return; }

        List<Solicitud> todas = solicitudDAO.listarTodos();
        List<Solicitud> filtradas = new ArrayList<>();

        for (Solicitud s : todas) {
            if (!texto.isEmpty()) {
                String cliente = s.getNombreCliente() != null ? s.getNombreCliente().toLowerCase() : "";
                String tec     = s.getNombreTecnico() != null ? s.getNombreTecnico().toLowerCase() : "";
                if (!cliente.contains(texto) && !tec.contains(texto)) continue;
            }
            if (fechaFiltro != null && !fechaFiltro.equals(s.getFechaSolicitada())) continue;
            filtradas.add(s);
        }

        filtradas.sort(Comparator
            .comparingInt((Solicitud s) -> {
                String e = s.getEstado() != null ? s.getEstado().toUpperCase() : "";
                switch (e) {
                    case "PENDIENTE":  return 0;
                    case "ASIGNADA":   return 1;
                    case "EN_CAMINO":  return 2;
                    case "EN_PROCESO": return 3;
                    case "COMPLETADA": return 4;
                    case "CANCELADA":   return 5;
                    default:           return 5;
                }
            })
            .thenComparingInt((Solicitud s) -> -pesoPrioridad(s.getPrioridad()))
        );

        tblSolicitudes.setItems(FXCollections.observableArrayList(filtradas));

        long pendCount = filtradas.stream()
            .filter(s -> "PENDIENTE".equalsIgnoreCase(s.getEstado())).count();
        long asigCount = filtradas.stream()
            .filter(s -> {
                String e = s.getEstado() != null ? s.getEstado().toUpperCase() : "";
                return e.equals("ASIGNADA") || e.equals("EN_CAMINO") || e.equals("EN_PROCESO");
            }).count();

        actualizarContadores((int) pendCount, (int) asigCount, filtradas.size());
    }

    @FXML public void limpiarFiltros() {
        if (dpFiltroFecha != null) dpFiltroFecha.setValue(null);
        if (txtBuscar != null) txtBuscar.clear();
        cargarTodosLosData();
    }

    // ════════════════════════════════════════════════════════
    //  SELECCIÓN DE FILA
    // ════════════════════════════════════════════════════════
    @FXML public void seleccionarFila() {
        Solicitud sel = tblSolicitudes.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        idSolicitudSeleccionada = sel.getIdSolicitud();
        String estado = sel.getEstado() != null ? sel.getEstado().toUpperCase() : "";
        esPendiente = "PENDIENTE".equals(estado);

        lblSolicitudSeleccionada.setText("#" + sel.getIdSolicitud() + " — " + sel.getNombreTipo());
        lblDireccionSolicitud.setText("📍 " + nvl(sel.getDireccionCliente(), "Sin dirección"));
        String fechaStr = sel.getFechaSolicitada() != null ? sel.getFechaSolicitada().toString() : "Sin fecha";
        lblFechaSolicitud.setText("📅 Fecha: " + fechaStr);
        lblHoraSolicitud.setText("🕐 Hora: " + nvl(sel.getHorarioPreferido(), "No indicada")
                + "  •  " + nvl(sel.getPrioridad(), ""));

        if (esPendiente) {
            btnAsignar.setDisable(false);
            sugerirTecnico(sel);
            cargarTecnicosLibresEnHora(sel);  // solo técnicos libres en esa hora
        } else {
            btnAsignar.setDisable(true);
            lblTecnicoSugerido.setText(nvl(sel.getNombreTecnico(), "—"));
            lblRazonSugerencia.setText("Estado actual: " + sel.getEstado());
            idAsignacionSeleccionada = asignacionDAO.buscarIdPorSolicitud(idSolicitudSeleccionada);
        }
    }

    // ════════════════════════════════════════════════════════
    //  ALGORITMO DE ASIGNACIÓN
    // ════════════════════════════════════════════════════════
    private void sugerirTecnico(Solicitud sol) {
        tecnicoSugerido = elegirMejorTecnico(sol, null);
        if (tecnicoSugerido != null) {
            lblTecnicoSugerido.setText(tecnicoSugerido.getNombreCompleto());
            StringBuilder r = new StringBuilder();
            if (tecnicoSugerido.getIdEspecialidad() == solicitudDAO.obtenerEspecialidadDeTipo(sol.getIdTipoServicio()))
                r.append("✅ Especialidad coincide  ");
            if (tecnicoSugerido.getIdZona() == solicitudDAO.obtenerZonaDeCliente(sol.getIdCliente()))
                r.append("📍 Zona coincide  ");
            r.append("🕐 Disponible en esa hora");
            lblRazonSugerencia.setText(r.toString());
        } else {
            lblTecnicoSugerido.setText("Sin técnicos disponibles");
            lblRazonSugerencia.setText("Todos los técnicos están ocupados en ese horario.");
        }
    }

    /**
     * Carga en el ComboBox solo los técnicos que están libres en la hora de la solicitud.
     * Si no hay ninguno disponible, deja el ComboBox vacío y muestra aviso.
     */
    private void cargarTecnicosLibresEnHora(Solicitud sol) {
        LocalTime horaSol  = parsearHora(sol.getHorarioPreferido());
        LocalDate fechaSol = sol.getFechaSolicitada() != null ? sol.getFechaSolicitada() : LocalDate.now();
        int diaSemana      = fechaSol.getDayOfWeek().getValue();

        Map<Integer, List<LocalTime[]>> ocupados = tecnicoDAO.obtenerOcupacionPorFecha(fechaSol);
        List<Tecnico> activos  = tecnicoDAO.listarActivos();
        List<Tecnico> libres   = new ArrayList<>();

        for (Tecnico t : activos) {
            // Verificar día laboral
            boolean[] dias = tecnicoDAO.cargarDiasHorario(t.getIdTecnico());
            if (!dias[diaSemana - 1]) continue;

            // Verificar franja horaria
            String[] horas = tecnicoDAO.cargarHorasHorario(t.getIdTecnico());
            LocalTime ini = parsearHora(horas[0]);
            LocalTime fin = parsearHora(horas[1]);
            if (horaSol == null || ini == null || fin == null) {
                libres.add(t); // sin hora definida, se incluye igual
                continue;
            }
            if (horaSol.isBefore(ini) || horaSol.isAfter(fin.minusHours(2))) continue;

            // Verificar que no tenga otra asignación solapada
            List<LocalTime[]> bloques = ocupados.getOrDefault(t.getIdTecnico(), new ArrayList<>());
            boolean solapado = false;
            for (LocalTime[] b : bloques) {
                if (horaSol.isBefore(b[1]) && horaSol.plusHours(3).isAfter(b[0])) {
                    solapado = true; break;
                }
            }
            if (!solapado) libres.add(t);
        }

        cmbTecnicoManual.setItems(FXCollections.observableArrayList(libres));
        cmbTecnicoManual.setValue(null);

        if (libres.isEmpty()) {
            mostrarMensaje("⚠️ No hay técnicos libres en ese horario.", false);
            btnAsignar.setDisable(true);
        } else {
            lblMensaje.setText("");
        }
    }

    private Tecnico elegirMejorTecnico(Solicitud sol, Map<Integer, List<LocalTime[]>> ocupadosExtra) {
        LocalTime horaSol  = parsearHora(sol.getHorarioPreferido());
        LocalDate fechaSol = sol.getFechaSolicitada() != null ? sol.getFechaSolicitada() : LocalDate.now();
        int diaSemana      = fechaSol.getDayOfWeek().getValue();

        List<Tecnico> activos = tecnicoDAO.listarActivos();
        int especNecesaria   = solicitudDAO.obtenerEspecialidadDeTipo(sol.getIdTipoServicio());
        int zonaCliente      = solicitudDAO.obtenerZonaDeCliente(sol.getIdCliente());

        Map<Integer, List<LocalTime[]>> ocupados = tecnicoDAO.obtenerOcupacionPorFecha(fechaSol);
        if (ocupadosExtra != null) {
            for (Map.Entry<Integer, List<LocalTime[]>> e : ocupadosExtra.entrySet())
                ocupados.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).addAll(e.getValue());
        }

        int mejorPuntaje = -1;
        Tecnico mejor    = null;

        for (Tecnico t : activos) {
            boolean[] dias = tecnicoDAO.cargarDiasHorario(t.getIdTecnico());
            if (!dias[diaSemana - 1]) continue;

            String[] horas = tecnicoDAO.cargarHorasHorario(t.getIdTecnico());
            LocalTime ini = parsearHora(horas[0]);
            LocalTime fin = parsearHora(horas[1]);
            if (horaSol == null || ini == null || fin == null) continue;
            if (horaSol.isBefore(ini) || horaSol.isAfter(fin.minusHours(2))) continue;

            List<LocalTime[]> bloques = ocupados.getOrDefault(t.getIdTecnico(), new ArrayList<>());
            boolean solapado = false;
            for (LocalTime[] b : bloques) {
                if (horaSol.isBefore(b[1]) && horaSol.plusHours(3).isAfter(b[0])) {
                    solapado = true; break;
                }
            }
            if (solapado) continue;

            int puntaje = 0;
            if (especNecesaria > 0 && t.getIdEspecialidad() == especNecesaria) puntaje += 2;
            if (zonaCliente    > 0 && t.getIdZona()         == zonaCliente)    puntaje += 1;

            if (puntaje > mejorPuntaje
             || (puntaje == mejorPuntaje && mejor != null && t.getIdTecnico() < mejor.getIdTecnico())) {
                mejorPuntaje = puntaje;
                mejor        = t;
            }
        }
        return mejor;
    }

    // ════════════════════════════════════════════════════════
    //  ASIGNACIÓN MASIVA
    // ════════════════════════════════════════════════════════
    @FXML public void asignarTodos() {
        List<Solicitud> pendientes = new ArrayList<>();
        for (Solicitud s : tblSolicitudes.getItems()) {
            if ("PENDIENTE".equalsIgnoreCase(s.getEstado())) pendientes.add(s);
        }
        pendientes.sort(Comparator.comparingInt((Solicitud s) -> pesoPrioridad(s.getPrioridad())).reversed());

        // Mapa de ocupación POR FECHA: permite manejar solicitudes de distintos días en el mismo batch.
        Map<LocalDate, Map<Integer, List<LocalTime[]>>> ocupadosPorFecha = new HashMap<>();

        int asignadas = 0, sinDisponible = 0;
        for (Solicitud sol : pendientes) {
            LocalDate fechaSol = sol.getFechaSolicitada() != null ? sol.getFechaSolicitada() : LocalDate.now();

            // Cargar ocupación real de BD para esa fecha solo la primera vez que aparece
            if (!ocupadosPorFecha.containsKey(fechaSol)) {
                ocupadosPorFecha.put(fechaSol,
                        new HashMap<>(tecnicoDAO.obtenerOcupacionPorFecha(fechaSol)));
            }
            Map<Integer, List<LocalTime[]>> ocupados = ocupadosPorFecha.get(fechaSol);

            Tecnico elegido = elegirMejorTecnico(sol, ocupados);
            if (elegido == null) { sinDisponible++; continue; }

            LocalTime hora = parsearHora(sol.getHorarioPreferido());
            Asignacion a = new Asignacion();
            a.setIdSolicitud(sol.getIdSolicitud());
            a.setIdTecnico(elegido.getIdTecnico());
            a.setTipoAsignacion("AUTOMATICA");
            a.setFechaProgramada(fechaSol.atTime(hora != null ? hora : LocalTime.of(8, 0)));
            a.setEstadoAsignacion("ASIGNADA");

            if (asignacionDAO.guardar(a)) {
                solicitudDAO.cambiarEstado(sol.getIdSolicitud(), "ASIGNADA");
                asignadas++;
                // Acumular bloqueo en el mapa de ESA fecha para las siguientes iteraciones del batch
                if (hora != null) {
                    ocupados.computeIfAbsent(elegido.getIdTecnico(), k -> new ArrayList<>())
                            .add(new LocalTime[]{hora, hora.plusHours(3)});
                }
            }
        }
        cargarTodosLosData();
        limpiarPanelSeleccion();

        String msg;
        boolean exito;
        if (asignadas == 0 && sinDisponible > 0) {
            msg   = "❌ No se pudo asignar ninguna solicitud.\n"
                  + "No hay técnicos disponibles para los " + sinDisponible + " horario(s) solicitado(s).\n"
                  + "Las solicitudes permanecen PENDIENTES.";
            exito = false;
        } else if (sinDisponible > 0) {
            msg   = "✅ " + asignadas + " solicitud(es) asignada(s).\n"
                  + "⚠️ " + sinDisponible + " solicitud(es) quedaron PENDIENTES por falta de técnicos disponibles en ese horario.";
            exito = false;
        } else {
            msg   = "✅ " + asignadas + " solicitud(es) asignada(s) correctamente.";
            exito = true;
        }
        // Toast además del label
        mostrarToast(exito ? "✔  Asignación automática exitosa" : (msg.startsWith("❌") ? "✖  Sin técnicos disponibles" : "⚠  Asignación parcial"), exito);
        mostrarMensaje(msg, exito);
    }

    // ════════════════════════════════════════════════════════
    //  CONFIRMAR ASIGNACIÓN INDIVIDUAL
    // ════════════════════════════════════════════════════════
    @FXML public void confirmarAsignacion() {
        if (idSolicitudSeleccionada < 0) return;
        Tecnico elegido = cmbTecnicoManual.getValue() != null ? cmbTecnicoManual.getValue() : tecnicoSugerido;
        if (elegido == null) { mostrarMensaje("Selecciona un técnico.", false); return; }

        Solicitud sol   = tblSolicitudes.getSelectionModel().getSelectedItem();
        LocalDate fecha = sol != null && sol.getFechaSolicitada() != null ? sol.getFechaSolicitada() : LocalDate.now();
        LocalTime hora  = sol != null ? parsearHora(sol.getHorarioPreferido()) : LocalTime.of(8, 0);

        Asignacion a = new Asignacion();
        a.setIdSolicitud(idSolicitudSeleccionada);
        a.setIdTecnico(elegido.getIdTecnico());
        a.setTipoAsignacion(cmbTecnicoManual.getValue() != null ? "MANUAL" : "AUTOMATICA");
        a.setFechaProgramada(fecha.atTime(hora != null ? hora : LocalTime.of(8, 0)));
        a.setEstadoAsignacion("ASIGNADA");
        a.setObservaciones(txtNotasAsignacion.getText());

        if (asignacionDAO.guardar(a)) {
            solicitudDAO.cambiarEstado(idSolicitudSeleccionada, "ASIGNADA");
            mostrarToast("✔  Asignación confirmada", true);
            mostrarMensaje("✅ Asignación confirmada.", true);
        } else {
            mostrarMensaje("❌ Error al guardar la asignación.", false);
        }
        cargarTodosLosData();
        limpiarPanelSeleccion();

    }


    // ════════════════════════════════════════════════════════
    //  UTILIDADES
    // ════════════════════════════════════════════════════════
    private LocalTime parsearHora(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try { return LocalTime.parse(hhmm.trim().substring(0, Math.min(5, hhmm.trim().length())),
                DateTimeFormatter.ofPattern("HH:mm")); }
        catch (Exception e) { return null; }
    }

    private void limpiarPanelSeleccion() {
        idSolicitudSeleccionada  = -1;
        idAsignacionSeleccionada = -1;
        tecnicoSugerido          = null;
        esPendiente              = false;
        lblSolicitudSeleccionada.setText("Selecciona una fila de la tabla");
        lblDireccionSolicitud.setText("");
        lblFechaSolicitud.setText("");
        lblHoraSolicitud.setText("");
        lblTecnicoSugerido.setText("—");
        lblRazonSugerencia.setText("");
        cmbTecnicoManual.setValue(null);
        txtNotasAsignacion.clear();
        btnAsignar.setDisable(true);
        lblMensaje.setText("");
    }

    private void actualizarContadores(int pend, int asig, int total) {
        lblPendientes.setText(String.valueOf(pend));
        lblAsignadas.setText(String.valueOf(asig));
        if (lblContador != null) lblContador.setText(total + " solicitud(es)");
        lblTecnicosLibres.setText(String.valueOf(tecnicoDAO.contarLibresHoy()));
    }

    private void mostrarMensaje(String texto, boolean exito) {
        lblMensaje.setText(texto);
        lblMensaje.setStyle(exito
            ? "-fx-text-fill: #2E7D32; -fx-font-size: 12px;"
            : "-fx-text-fill: #C62828; -fx-font-size: 12px;");
    }

    private String nvl(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

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