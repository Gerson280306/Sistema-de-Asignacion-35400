package Modelo;

public class Tecnico {
    private int idTecnico;
    private String dni, nombres, apellidos, telefono, email;
    private int idEspecialidad;
    private String nombreEspecialidad;
    private int idZona;
    private String nombreZona;
    private String nivel, disponibilidad;
    private int maxSolicitudesDia;
    private String observaciones;
    private int estado;

    public int getIdTecnico() { return idTecnico; }
    public void setIdTecnico(int v) { idTecnico = v; }
    public String getDni() { return dni; }
    public void setDni(String v) { dni = v; }
    public String getNombres() { return nombres; }
    public void setNombres(String v) { nombres = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { apellidos = v; }
    public String getNombreCompleto() { return nombres + " " + apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { telefono = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public int getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(int v) { idEspecialidad = v; }
    public String getNombreEspecialidad() { return nombreEspecialidad; }
    public void setNombreEspecialidad(String v) { nombreEspecialidad = v; }
    public int getIdZona() { return idZona; }
    public void setIdZona(int v) { idZona = v; }
    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String v) { nombreZona = v; }
    public String getNivel() { return nivel; }
    public void setNivel(String v) { nivel = v; }
    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String v) { disponibilidad = v; }
    public int getMaxSolicitudesDia() { return maxSolicitudesDia; }
    public void setMaxSolicitudesDia(int v) { maxSolicitudesDia = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { observaciones = v; }
    public int getEstado() { return estado; }
    public void setEstado(int v) { estado = v; }
    @Override public String toString() { return getNombreCompleto(); }
}
