# Sistema de Asignación

Sistema de escritorio desarrollado en **JavaFX** para la gestión y asignación de solicitudes de servicio a técnicos, considerando especialidad, zona, disponibilidad y validaciones de negocio.

El proyecto sigue una estructura inspirada en **MVC**, separando:

- **Vista**: interfaces JavaFX (`.fxml`)
- **Controlador**: lógica de interacción y validaciones
- **Modelo**: entidades del sistema
- **DAO**: acceso a datos mediante MySQL
- **Conexión**: manejo centralizado de la conexión a base de datos

## Funcionalidades principales

- Inicio de sesión de usuarios.
- Gestión de clientes.
- Gestión de técnicos.
- Registro y administración de solicitudes.
- Asignación manual y automática de solicitudes.
- Consulta de historial.
- Generación de reportes.
- Administración de cuentas de usuario.
- Panel principal y panel administrativo.

## Tecnologías utilizadas

- **Java**
- **JavaFX**
- **MySQL**
- **JDBC**
- **NetBeans / Ant**
- Librerías externas incluidas en `dist/lib/`

## Estructura del proyecto

```text
src/
├── Conexion/
├── Controlador/
├── DAO/
├── Main/
├── Modelo/
└── Vista/
```

### Vistas disponibles

- `LoginView.fxml`
- `MenuPrincipalView.fxml`
- `MenuAdminView.fxml`
- `ClienteView.fxml`
- `TecnicoView.fxml`
- `SolicitudView.fxml`
- `AsignacionView.fxml`
- `HistorialView.fxml`
- `ReporteView.fxml`
- `AdminCuentasView.fxml`
- `AdminReportesView.fxml`
- `FX.fxml`

### Controladores principales

- `LoginController`
- `MenuPrincipalController`
- `MenuAdminController`
- `ClienteController`
- `TecnicoController`
- `SolicitudController`
- `AsignacionController`
- `HistorialController`
- `ReporteController`
- `AdminCuentasController`
- `AdminReportesController`

### Modelos principales

- `Usuario`
- `Cliente`
- `Tecnico`
- `Solicitud`
- `Asignacion`
- `Especialidad`
- `TipoServicio`
- `Zona`

## Base de datos

El script de base de datos se encuentra en:

`Base de Datos/Base de Datos.sql`

Tablas principales:

- `tb_usuario`
- `tb_cliente`
- `tb_tecnico`
- `tb_solicitud`
- `tb_asignacion`
- `tb_tipo_servicio`
- `tb_especialidad`
- `tb_zona`
- `tb_horario`

## Requisitos previos

- Java instalado.
- MySQL instalado y en ejecución.
- Un entorno compatible con JavaFX.
- Editor o IDE como NetBeans, IntelliJ IDEA o VS Code con soporte Java.

## Configuración de la base de datos

Antes de ejecutar el sistema, revisa la clase:

`src/Conexion/ConexionDB.java`

Ahí puedes ajustar:

- host
- puerto
- nombre de la base de datos
- usuario
- contraseña

Actualmente la conexión está configurada para una base MySQL local.

## Instalación y ejecución

1. Clona o descomprime el proyecto.
2. Importa el script `Base de Datos.sql` en MySQL.
3. Verifica que el nombre de la base coincida con la configurada en `ConexionDB.java`.
4. Abre el proyecto en tu IDE.
5. Ejecuta la clase principal:

   `src/Main/Main.java`

6. El sistema iniciará en la pantalla de login.

## Flujo general del sistema

1. El usuario inicia sesión.
2. Según su rol, accede al menú principal o al menú administrativo.
3. Se registran y consultan clientes, técnicos y solicitudes.
4. El sistema permite asignar técnicos a solicitudes de forma manual o automática.
5. Se consultan estados, historial y reportes.

## Notas importantes

- El sistema valida datos de entrada antes de guardar o actualizar registros.
- La asignación toma en cuenta criterios como especialidad, zona y disponibilidad del técnico.
- El proyecto ya incluye dependencias compiladas dentro de `dist/lib/`.
- El archivo `dist/Sistema_de_Asignacion.jar` permite ejecutar una versión compilada del sistema.

## Autoría

Proyecto académico de sistema de asignación para gestión de solicitudes y técnicos.
