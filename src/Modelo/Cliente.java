package Modelo;

public class Cliente {
    private int idCliente;
    private String dni, nombres, apellidos, telefono, email;
    private String direccion, referencia, distrito;
    private int idZona;
    private String nombreZona;
    private String observaciones;
    private int estado;

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) { idCliente = v; }
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
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { direccion = v; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { referencia = v; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String v) { distrito = v; }
    public int getIdZona() { return idZona; }
    public void setIdZona(int v) { idZona = v; }
    public String getNombreZona() { return nombreZona; }
    public void setNombreZona(String v) { nombreZona = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { observaciones = v; }
    public int getEstado() { return estado; }
    public void setEstado(int v) { estado = v; }
    @Override public String toString() { return getNombreCompleto(); }
}
