package Modelo;

public class Especialidad {
    private int idEspecialidad;
    private String nombre;

    public Especialidad() {}
    public Especialidad(int id, String nombre) { this.idEspecialidad = id; this.nombre = nombre; }
    public int getIdEspecialidad() { return idEspecialidad; }
    public void setIdEspecialidad(int v) { idEspecialidad = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { nombre = v; }
    @Override public String toString() { return nombre; }
}
