package Modelo;
import java.time.LocalDate;

public class Solicitud {
    private int idSolicitud;
    private String codigo;
    private int idCliente;
    private String nombreCliente;
    private int idTipoServicio;
    private String nombreTipo;
    private String descripcion, prioridad;
    private String direccionAtencion, referenciaAtencion;
    private LocalDate fechaSolicitada;
    private String horarioPreferido; // "HH:MM"
    private String estado, observaciones;
    private String nombreTecnico;

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int v) { idSolicitud = v; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { codigo = v; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) { idCliente = v; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String v) { nombreCliente = v; }
    public int getIdTipoServicio() { return idTipoServicio; }
    public void setIdTipoServicio(int v) { idTipoServicio = v; }
    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String v) { nombreTipo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { descripcion = v; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String v) { prioridad = v; }
    public String getDireccionAtencion() { return direccionAtencion; }
    public void setDireccionAtencion(String v) { direccionAtencion = v; }
    public String getReferenciaAtencion() { return referenciaAtencion; }
    public void setReferenciaAtencion(String v) { referenciaAtencion = v; }
    public LocalDate getFechaSolicitada() { return fechaSolicitada; }
    public void setFechaSolicitada(LocalDate v) { fechaSolicitada = v; }
    public String getHorarioPreferido() { return horarioPreferido; }
    public void setHorarioPreferido(String v) { horarioPreferido = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { estado = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { observaciones = v; }
    public String getNombreTecnico() { return nombreTecnico; }
    public void setNombreTecnico(String v) { nombreTecnico = v; }
}
