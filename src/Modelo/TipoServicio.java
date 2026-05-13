package Modelo;

public class TipoServicio {
    private int idTipoServicio;
    private String nombre;

    public TipoServicio() {}
    public TipoServicio(int id, String nombre) { this.idTipoServicio = id; this.nombre = nombre; }
    public int getIdTipoServicio() { return idTipoServicio; }
    public void setIdTipoServicio(int v) { idTipoServicio = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = v; }
    @Override public String toString() { return nombre; }
}
