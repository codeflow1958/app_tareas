# Sistema de Gestión de Tareas (Backend)

Este es un sistema de gestión de tareas RESTful enfocado en la implementación de estructuras de datos personalizadas y la comunicación asíncrona. Inspirado en herramientas como Trello o Todoist, este proyecto demuestra cómo construir un backend robusto y modular para la administración de tareas, con funcionalidades como jerarquía de tareas, deshacer acciones y programación de tareas.

---

## Tecnologías Utilizadas

* **Java 18:** Lenguaje de programación principal.
* **Maven:** Herramienta para la gestión de proyectos y dependencias.
* **Spring Boot 3.x:** Framework para construir la aplicación RESTful.
* **MySQL:** Base de datos relacional para la persistencia de las tareas.
* **MongoDB (v6.x):** Base de datos NoSQL utilizada para el registro de logs y eventos históricos.
* **RabbitMQ (v3.x):** Sistema de mensajería asíncrona, gestionado vía Docker, para la comunicación de eventos.
* **Docker:** Para levantar y gestionar los servicios de MySQL y RabbitMQ/MongoDB de forma aislada.
* **Swagger (SpringDoc OpenAPI):** Para la documentación interactiva y prueba de las APIs REST.

---

## Estructuras de Datos Personalizadas (Implementadas desde Cero)

Este proyecto hace un énfasis particular en la implementación de las siguientes estructuras de datos, sin depender de las colecciones integradas de Java:

* **Pila (Stack):** Utilizada para la implementación de la funcionalidad de **"deshacer"** acciones. Permite revertir la última operación realizada (creación, actualización, eliminación de tareas y subtareas).
* **Árbol (Tree):** Utilizado para gestionar la **jerarquía de tareas** (tareas padre y subtareas). Permite organizar las tareas en niveles y reconstruye su estado en memoria a partir de la base de datos al inicio de la aplicación.
* **Cola (Queue):** Implementada para la gestión de **tareas programadas**. Permite encolar tareas para ser procesadas en un orden FIFO (Primero en Entrar, Primero en Salir).
* **Lista (Linked List):** (Aquí puedes mencionar si la implementaste o si quedó como un objetivo futuro de la asignatura).

---

##  Estructura del Proyecto

El proyecto está organizado en una arquitectura de **módulos Maven separados** para promover la modularidad y el desacoplamiento:

1.  **`gestion-tareas-app`**:
    * Aplicación principal Spring Boot.
    * Expone las APIs REST.
    * Contiene la lógica de negocio (servicios) y los controladores.
    * Maneja la configuración de la base de datos y la mensajería.
    * **Depende de `estructuras-datos` y `gestion-tareas-persistencia` (si se usó para MongoDB).**
2.  **`estructuras-datos`**:
    * Módulo JAR independiente que contiene la implementación de las estructuras de datos personalizadas (Pila, Árbol, Cola, Lista).
    * Es una dependencia para `gestion-tareas-app`.
3.  **`gestion-tareas-persistencia`**:
    * (Si lo hubiéramos usado para la persistencia de tareas, aquí iría `Tarea.java` y `TareaRepositoryMySQL.java`).
    * **Estado Actual:** En este proyecto, para simplificar el despliegue, las entidades y repositorios de MySQL y MongoDB se mantuvieron directamente en `gestion-tareas-app`. Este repositorio existe pero no está activo en esta versión.

---

##  Cómo Iniciar el Proyecto

Para levantar el proyecto y sus dependencias, sigue estos pasos:

1.  **Clonar los Repositorios:**
    * Asegúrate de tener los tres repositorios clonados en tu máquina:
        ```bash
        git clone <URL_TU_REPO_GESTION_TAREAS_APP> gestion-tareas-app
        git clone <URL_TU_REPO_ESTRUCTURAS_DATOS> estructuras-datos
        # git clone <URL_TU_REPO_PERSISTENCIA> gestion-tareas-persistencia # Si lo tuvieras, clonarías este también
        ```
    * Reemplaza `<URL_TU_REPO...>` con las URLs reales de tus repositorios en GitHub.

2.  **Configurar Docker:**
    * Asegúrate de tener [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en ejecución.
    * Abre tu terminal y levanta los contenedores de MySQL y RabbitMQ (y MongoDB):
        ```bash
        # Iniciar MySQL (si lo tienes en Docker)
        docker run -d --name my-mysql-container -p 3306:3306 -e MYSQL_ROOT_PASSWORD=tu_contraseña_de_root mysql:latest

        # Iniciar RabbitMQ
        docker run -d --hostname my-rabbitmq --name my-rabbitmq-container -p 5672:5672 -p 15672:15672 rabbitmq:3-management

        # Iniciar MongoDB
        docker run -d --name my-mongodb-container -p 27017:27017 mongo:latest
        ```
    * Verifica que estén corriendo: `docker ps`

3.  **Configurar la Base de Datos:**
    * Crea la base de datos `finalproyect` en tu servidor MySQL.
        ```sql
        CREATE DATABASE finalproyect;
        ```
    * Asegúrate de que las credenciales en `gestion-tareas-app/src/main/resources/application.properties` coincidan con tu configuración de MySQL y MongoDB.
        ```properties
        # ... configuración de MySQL ...
        spring.datasource.url=jdbc:mysql://localhost:3306/finalproyect
        spring.datasource.username=root
        spring.datasource.password=tu_contraseña_de_root
        spring.jpa.hibernate.ddl-auto=update # Importante para la creación/actualización automática de tablas

        # ... configuración de RabbitMQ ...

        # Configuración de MongoDB
        spring.data.mongodb.host=localhost
        spring.data.mongodb.port=27017
        spring.data.mongodb.database=gestion_tareas_logs
        ```

4.  **Construir e Instalar los Módulos de Dependencia:**
    * Abre tu terminal y navega al directorio `estructuras-datos`.
    * Ejecuta:
        ```bash
        mvn clean install
        ```
    * (Si tuvieras `gestion-tareas-persistencia` con algo, harías lo mismo allí).

5.  **Abrir y Ejecutar el Proyecto Principal en IntelliJ IDEA:**
    * Abre la carpeta `gestion-tareas-app` como un proyecto en IntelliJ IDEA.
    * En la ventana de herramientas de Maven (a la derecha), haz clic en "Reimport All Maven Projects".
    * Ve a `Build` > `Clean Project` y luego `Build` > `Rebuild Project`.
    * Ejecuta la clase `GestionTareasApplication.java`.

---

## Pruebas y Endpoints de la API

Una vez que la aplicación esté en ejecución, puedes acceder a la interfaz de Swagger UI para probar los endpoints:

**URL de Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

**Endpoints Principales:**

* **Gestión de Tareas (CRUD):**
    * `GET /api/tareas`: Obtener todas las tareas.
    * `GET /api/tareas/{id}`: Obtener una tarea por ID.
    * `POST /api/tareas`: Crear una nueva tarea.
    * `PUT /api/tareas/{id}`: Actualizar una tarea.
    * `DELETE /api/tareas/{id}`: Eliminar una tarea.
* **Funcionalidad de Deshacer:**
    * `POST /api/tareas/deshacer`: Deshacer la última operación de tarea (creación, actualización, eliminación).
* **Jerarquía de Tareas (Árbol):**
    * `POST /api/tareas/{idPadre}/subtarea`: Crear una subtarea asociada a un padre.
    * `GET /api/tareas/jerarquia`: Obtener todas las tareas del árbol (lista plana de la jerarquía).
* **Tareas Programadas (Cola):**
    * `POST /api/tareas/programar`: Agregar una tarea a la cola de programadas.
    * `POST /api/tareas/procesar-siguiente`: Procesar la siguiente tarea de la cola.
    * `GET /api/tareas/siguiente-programada`: Ver la tarea en el frente de la cola sin procesarla.
    * `GET /api/tareas/cola-vacia`: Verificar si la cola está vacía.

**Observar Logs:**

* Revisa la consola de tu aplicación en IntelliJ IDEA para ver los logs de actividad de las estructuras de datos, RabbitMQ, MySQL y MongoDB.
* Accede a la interfaz de administración de RabbitMQ (`http://localhost:15672/` - guest/guest) para ver los mensajes publicados en el exchange y las colas.
* Usa MongoDB Compass (`mongodb://localhost:27017`) para ver los documentos `event_logs` guardados en MongoDB.
