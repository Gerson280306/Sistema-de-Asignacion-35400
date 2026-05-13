package Modelo;

public class Zona {
    private int idZona;
    private String nombre;

    public Zona() {}
    public Zona(int id, String nombre) { this.idZona = id; this.nombre = nombre; }
    public int getIdZona() { return idZona; }
    public void setIdZona(int v) { idZona = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = v; }
    @Override public String toString() { return nombre; }
}
