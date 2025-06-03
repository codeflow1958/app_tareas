# Sistema de Gestión de Tareas (Backend) 

¡Bienvenido al backend de un sistema de gestión de tareas!  
Este proyecto es una implementación desde cero, inspirada en herramientas como **Trello** o **Todoist**, pero con un enfoque profundo en la aplicación de **estructuras de datos personalizadas** y la **comunicación asíncrona**.

---

## Características Principales

- **Gestión Completa de Tareas:** Crea, edita, elimina y marca tareas como completadas.
- **Jerarquía de Tareas:** Organiza tus tareas en subniveles (padre/hijo) usando un árbol en memoria.
- **Funcionalidad "Deshacer":** Reversa la última operación con una pila.
- **Tareas Programadas:** Procesa tareas en orden FIFO usando una cola.
- **APIs RESTful:** Todas las operaciones disponibles mediante una API bien definida.
- **Registro de Eventos:** Historial detallado en MongoDB.
- **Comunicación Asíncrona:** RabbitMQ para eventos desacoplados.

---

## Tecnologías y Herramientas

| Herramienta         | Uso                                           |
|---------------------|-----------------------------------------------|
| Java 18             | Lenguaje principal                            |
| Maven               | Gestión de dependencias                       |
| Spring Boot 3.x     | Framework principal                           |
| MySQL               | Base de datos relacional                      |
| MongoDB             | Logs/eventos históricos (NoSQL)               |
| RabbitMQ            | Comunicación de eventos                       |
| Docker              | Contenedores para servicios                   |
| Swagger             | Documentación interactiva de APIs            |
| IntelliJ IDEA       | IDE recomendado                               |

---

## Estructuras de Datos Personalizadas

### Pila – `com.umg.estructuras.pila.PilaAcciones`
- Implementación LIFO sin `java.util.*`.
- Uso: funcionalidad "deshacer".

### Árbol – `com.umg.estructuras.arbol.ArbolJerarquicoTareas`
- Árbol general de múltiples hijos.
- Uso: mantiene jerarquía de tareas en memoria.

### Cola – `com.umg.estructuras.cola.ColaTareasProgramadas`
- Cola FIFO usando nodos enlazados.
- Uso: tareas en espera de ser ejecutadas.

### Lista – `com.umg.estructuras.lista.ListaTarea`
- Lista enlazada simple.
- Uso potencial: tareas favoritas o vistas recientemente.

---

## Estructura de Módulos Maven

```plaintext
gestion-tareas-app          // Aplicación Spring Boot principal
└── estructuras-datos       // Librería con estructuras de datos
└── gestion-tareas-persistencia (opcional/inactivo)
```

---

## Cómo Poner en Marcha el Proyecto

### Requisitos Previos:
- Java 18
- Maven
- Docker Desktop
- IntelliJ IDEA

### 1️Clonar los Repositorios

```bash
git clone https://github.com/tu-usuario/gestion-tareas-app.git
git clone https://github.com/tu-usuario/estructuras-datos.git
# git clone https://github.com/tu-usuario/gestion-tareas-persistencia.git
```

> Asegúrate de tener todos los módulos en la misma carpeta raíz.

---

### 2️⃣ Levantar Contenedores con Docker

```bash
# MySQL
docker run -d --name my-mysql-container -p 3306:3306 -e MYSQL_ROOT_PASSWORD=tu_contraseña_de_root mysql:latest

# RabbitMQ
docker run -d --hostname my-rabbitmq --name my-rabbitmq-container -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# MongoDB
docker run -d --name my-mongodb-container -p 27017:27017 mongo:latest
```

---

### 3️⃣ Crear Base de Datos MySQL

```sql
CREATE DATABASE finalproyect;
```

---

### 4️⃣ Configurar `application.properties`

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/finalproyect
spring.datasource.username=root
spring.datasource.password=tu_contraseña_de_root
spring.jpa.hibernate.ddl-auto=update

# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=gestion_tareas_logs
```

---

### 5️⃣ Instalar el Módulo de Estructuras de Datos

```bash
cd estructuras-datos
mvn clean install
```

---

### 6️⃣ Ejecutar el Proyecto en IntelliJ IDEA

1. Abrir `gestion-tareas-app` como proyecto Maven.
2. Reimportar todos los proyectos Maven.
3. Ir a `Build > Clean Project` y luego `Rebuild Project`.
4. Ejecutar `com.umg.gestiontareas.GestionTareasApplication`.

---

## ¡A Probar la API con Swagger!

> Accede a: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

###  CRUD de Tareas

```http
GET /api/tareas
POST /api/tareas
PUT /api/tareas/{id}
DELETE /api/tareas/{id}
```

### Deshacer

```http
POST /api/tareas/deshacer
```

### Jerarquía

```http
POST /api/tareas/{idPadre}/subtarea
GET /api/tareas/jerarquia
```

### Tareas Programadas (Cola)

```http
POST /api/tareas/programar
GET /api/tareas/siguiente-programada
POST /api/tareas/procesar-siguiente
GET /api/tareas/cola-vacia
```

---

## ¡No te Pierdas los Logs!

- **Consola de IntelliJ:** muestra las estructuras en acción.
- **RabbitMQ UI:** [http://localhost:15672](http://localhost:15672)
  - Usuario: `guest` — Contraseña: `guest` (por defecto).
- **MongoDB Compass**: conecta a `mongodb://localhost:27017` y selecciona la base `gestion_tareas_logs`.

