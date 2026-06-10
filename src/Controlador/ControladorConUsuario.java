package Controlador;

import Modelo.Usuario;

/**
 * Interfaz que implementan los controladores de menú
 * para recibir el usuario autenticado desde LoginController.
 */
public interface ControladorConUsuario {
    void setUsuarioActual(Usuario usuario);
}
