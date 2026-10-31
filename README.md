# DesafioPractico3-DSM
[![My Skills](https://skillicons.dev/icons?i=kotlin,nodejs,&theme=dark)](https://skillicons.dev)
### Desarrollo de aplicacion con multiples pantallas con roles de usuarios y uso de node.js en parte backend para la api de la aplicacion y haciendo deploy en el webservice de [Render.com](https://dashboard.render.com/)

[Repositorio de Api de la aplicacion android /Recursos](https://github.com/YCartaArt02/api-dsm3)
<br>
[Enlace de Video sobre el funcionamiento de la aplicacion](https://drive.google.com/file/d/1cU6LSq8pxYZq4phmNTHycfIwbFdjqCSA/view?usp=sharing)


##  Pantallas de la Aplicación

La interfaz gráfica fue diseñada utilizando un estilo moderno de **Liquid Glass (Glassmorphism)** nativo, implementando fondos con gradientes dinámicos, tarjetas semitransparentes, bordes brillantes y barras de navegación tipo "píldora flotante" para una experiencia inmersiva y fluida.

La aplicación se divide según el rol del usuario (Autenticación basada en Firebase):

### 🔐 Módulo de Autenticación
* **Iniciar Sesión (Login):** Interfaz limpia donde el usuario ingresa sus credenciales. Cuenta con validación de estado de sesión persistente (si el usuario ya se había logueado, la app detecta su rol en Firestore y lo redirige automáticamente a su panel sin pedir contraseña).
* **Registro de Usuario:** Formulario para crear una nueva cuenta. Incluye campos con validación, confirmación de contraseña y un selector de rol (Docente o Alumno) que configura los permisos del usuario en la base de datos.

### 👨‍🏫 Módulo del Docente (Gestión)
* **Panel Principal (Dashboard Docente):** Muestra la lista completa de recursos educativos obtenidos desde la API REST. 
  * Cuenta con una **barra de búsqueda en tiempo real** (oculta tras un botón circular de cristal) que permite filtrar la lista al instante por nombre o tipo de recurso.
  * Incluye funcionalidad *Swipe-to-Refresh* para recargar los datos y un botón flotante (FAB) para agregar nuevo contenido.
  * Cada tarjeta permite al docente **Editar** o **Eliminar** el recurso.
* **Formulario de Recurso:** Pantalla envuelta en un panel de cristal esmerilado donde el docente ingresa los detalles del nuevo material (Título, Tipo mediante menú desplegable, Enlace URL y Descripción).

### 🎓 Módulo del Alumno (Exploración)
* **Panel de Exploración (Dashboard Alumno):** Permite a los estudiantes visualizar todos los recursos disponibles. 
  * Hereda el buscador en tiempo real para encontrar materiales rápidamente.
  * Cada tarjeta de recurso cuenta con un botón para **Abrir Enlace** (redirige al material interactivo/URL), un botón para agregar a **Favoritos**, y un sistema integrado de **Valoración (RatingBar)** para calificar el contenido.
* **Mis Favoritos:** Una sección dedicada donde el alumno puede ver únicamente los recursos que ha marcado con el corazón. Cuenta con una barra de navegación tipo píldora para regresar fácilmente al panel principal y manejo de estados vacíos (mensaje cuando aún no hay favoritos guardados).
