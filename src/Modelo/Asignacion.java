package Modelo;

import java.time.LocalDateTime;

/**
 * Modelo para tb_asignacion.
 */
public class Asignacion {
    private int           idAsignacion;
    private int           idSolicitud;
    private int           idTecnico;
    private String        tipoAsignacion;    // AUTOMATICA | MANUAL
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaInicioReal;
    private LocalDateTime fechaFinReal;
    private String        estadoAsignacion;  // ASIGNADA | EN_CAMINO | EN_PROCESO | COMPLETADA | CANCELADA
    private String        observaciones;

    public int           getIdAsignacion()     { return idAsignacion; }
    public void          setIdAsignacion(int v){ idAsignacion = v; }
    public int           getIdSolicitud()      { return idSolicitud; }
    public void          setIdSolicitud(int v) { idSolicitud = v; }
    public int           getIdTecnico()        { return idTecnico; }
    public void          setIdTecnico(int v)   { idTecnico = v; }
    public String        getTipoAsignacion()   { return tipoAsignacion; }
    public void          setTipoAsignacion(String v) { tipoAsignacion = v; }
    public LocalDateTime getFechaProgramada()  { return fechaProgramada; }
    public void          setFechaProgramada(LocalDateTime v) { fechaProgramada = v; }
    public String        getEstadoAsignacion() { return estadoAsignacion; }
    public void          setEstadoAsignacion(String v) { estadoAsignacion = v; }
    public String        getObservaciones()    { return observaciones; }
    public void          setObservaciones(String v) { observaciones = v; }
}
