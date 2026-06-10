package Modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modelo.Usuario — representa un registro de tb_usuario.
 */
public class Usuario {

    private int idUsuario;
    private String username;
    private String nombreCompleto;
    private String email;
    private String rol;          // ADMIN | SUPERVISOR | OPERADOR
    private int estado;          // 1 = activo, 0 = inactivo
    private LocalDateTime ultimoAcceso;
    private LocalDateTime fechaCreacion;

    public Usuario() {}

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public int getIdUsuario()               { return idUsuario; }
    public void setIdUsuario(int v)         { this.idUsuario = v; }

    public String getUsername()             { return username; }
    public void setUsername(String v)       { this.username = v; }

    public String getNombreCompleto()       { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }

    public String getEmail()                { return email != null ? email : ""; }
    public void setEmail(String v)          { this.email = v; }

    public String getRol()                  { return rol; }
    public void setRol(String v)            { this.rol = v; }

    public int getEstado()                  { return estado; }
    public void setEstado(int v)            { this.estado = v; }

    public LocalDateTime getUltimoAcceso()        { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime v)  { this.ultimoAcceso = v; }

    public LocalDateTime getFechaCreacion()       { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime v) { this.fechaCreacion = v; }

    // ─── Helpers para TableView ───────────────────────────────────────────────

    public String getEstadoTexto()    { return estado == 1 ? "Activo" : "Inactivo"; }

    public String getUltimoAccesoStr() {
        if (ultimoAcceso == null) return "Nunca";
        return ultimoAcceso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFechaCreacionStr() {
        if (fechaCreacion == null) return "";
        return fechaCreacion.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public String toString() { return nombreCompleto + " (" + username + ")"; }
}
