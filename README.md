# Taller: Implementación de OpenTelemetry en Microservicios

Dos microservicios Spring Boot instrumentados con OpenTelemetry usando tres enfoques diferentes.

## Integrantes

- Juan David Calderón
- Alejandro Amu
- David Henao
- Alejandro Torres Soto

## Evidencias

[Capturas-OpenTelemetry.pdf](Capturas-OpenTelemetry.pdf)

## Servicios

| Servicio                 | Puerto | Descripción                                |
| ------------------------ | ------ | ------------------------------------------ |
| **FutureXCourseApp**     | 8001   | CRUD de cursos con base de datos MySQL     |
| **FutureXCourseCatalog** | 8002   | Catálogo que consulta a CourseApp vía HTTP |

## Estructura del proyecto

```
Taller-Open-Telemetry/
├── parte1-JaegerCourseApp/      # Parte 1: app limpia para el Agente Java
├── parte1-JaegerCourseCatalog/  # Parte 1: app limpia para el Agente Java
├── parte0-JaegerCourseApp/      # Partes 2 y 3: instrumentación manual con API OTel
├── part0-JaegerCourseCatalog/   # Partes 2 y 3: instrumentación manual con API OTel
├── docker-compose.yml           # Infraestructura: Jaeger + OTel Collector
├── otel-collector-config.yaml   # Configuración del Collector (Parte 3)
└── download-agent.sh            # Script para descargar el agente Java (Parte 1)
```

---

## Requisitos previos

- Java 17
- Maven 3.8+
- Docker y Docker Compose
- MySQL 8 corriendo en `localhost:3306` (usuario: `root`, contraseña: `techbankRootPsw`)

---

## Parte 1: Agente Java de OpenTelemetry

**Concepto:** cero cambios de código. El agente instrumenta automáticamente Spring MVC, RestTemplate, JDBC, etc.

### Pasos

**1. Levantar Jaeger**

```bash
docker compose up jaeger -d
```

> Jaeger UI disponible en: http://localhost:16686

**2. Descargar el agente**

```bash
./download-agent.sh
```

**3. Compilar ambas aplicaciones**

```bash
cd parte1-JaegerCourseApp && mvn package -DskipTests && cd ..
cd parte1-JaegerCourseCatalog && mvn package -DskipTests && cd ..
```

**4. Ejecutar con el agente**

En dos terminales separadas:

```bash
# Terminal 1 - CourseApp
java -javaagent:opentelemetry-javaagent.jar \
     -jar parte1-JaegerCourseApp/target/FutureXCourseApp-0.0.1-SNAPSHOT.jar

# Terminal 2 - CourseCatalog
java -javaagent:opentelemetry-javaagent.jar \
     -jar parte1-JaegerCourseCatalog/target/FutureXCourseCatalog-0.0.1-SNAPSHOT.jar
```

**5. Ejercitar los endpoints**

```bash
curl http://localhost:8002/catalog
curl http://localhost:8002/firstcourse
```

**6. Verificar trazas en Jaeger**

- Abrir http://localhost:16686
- Seleccionar servicio `fx-catalog-service`
- Las trazas mostrarán spans automáticos de HTTP y JDBC, con propagación distribuida entre servicios

---

## Parte 2: API Manual de OpenTelemetry

**Concepto:** se usa el SDK de OTel directamente en el código para crear spans personalizados.

### Archivos clave

- [OpenTelemetryConfig.java](parte0-JaegerCourseApp/src/main/java/com/futurex/services/FutureXCourseApp/OpenTelemetryConfig.java) — configura el SDK: exporter OTLP, TracerProvider, propagador W3C
- [CourseController.java](parte0-JaegerCourseApp/src/main/java/com/futurex/services/FutureXCourseApp/CourseController.java) — spans manuales en cada endpoint
- [CatalogController.java](part0-JaegerCourseCatalog/src/main/java/com/futurex/services/FutureXCourseCatalog/CatalogController.java) — spans manuales en cada endpoint
- [FutureXCourseCatalogApplication.java](part0-JaegerCourseCatalog/src/main/java/com/futurex/services/FutureXCourseCatalog/FutureXCourseCatalogApplication.java) — `RestTemplate` con interceptor W3C para propagar el contexto de traza entre servicios

### Pasos

**1. Configurar endpoint directo a Jaeger**

En ambos `application.properties` (parte0), verificar que el endpoint apunte a Jaeger:

```properties
otel.exporter.otlp.endpoint=http://localhost:4317
```

**2. Levantar Jaeger**

```bash
docker compose up jaeger -d
```

**3. Ejecutar las aplicaciones**

```bash
# Terminal 1
cd parte0-JaegerCourseApp && mvn spring-boot:run

# Terminal 2
cd part0-JaegerCourseCatalog && mvn spring-boot:run
```

**4. Verificar trazas en Jaeger**

- Abrir http://localhost:16686
- Las trazas de `fx-catalog-service` mostrarán el span de Catalog conectado con los spans de CourseApp en la misma traza distribuida (gracias al interceptor W3C en RestTemplate)

---

## Parte 3: Colector de OpenTelemetry

**Concepto:** las aplicaciones envían trazas al **OTel Collector** en lugar de ir directamente a Jaeger. El Collector procesa y reenvía a Jaeger (o a múltiples backends).

```
[Apps] --OTLP--> [Collector :4319] --OTLP--> [Jaeger :4317]
```

### Archivos clave

- [otel-collector-config.yaml](otel-collector-config.yaml) — define receiver, processor (batch) y exporter del Collector
- [docker-compose.yml](docker-compose.yml) — levanta Jaeger + Collector con los puertos correctos
- `application.properties` (parte0) con `otel.exporter.otlp.endpoint=http://localhost:4319`

### Pasos

**1. Levantar toda la infraestructura**

```bash
docker compose up -d
```

> Esto inicia Jaeger (16686, 4317) y el OTel Collector (4319 → 4317)

**2. Verificar el endpoint en application.properties**

Ambos servicios deben apuntar al Collector (puerto 4319):

```properties
otel.exporter.otlp.endpoint=http://localhost:4319
```

**3. Ejecutar las aplicaciones (mismos archivos que Parte 2)**

```bash
# Terminal 1
cd parte0-JaegerCourseApp && mvn spring-boot:run

# Terminal 2
cd part0-JaegerCourseCatalog && mvn spring-boot:run
```

**4. Verificar trazas en Jaeger**

- Abrir http://localhost:16686
- Las trazas llegan a Jaeger habiendo pasado por el Collector

---

## Comparativa de enfoques

|                      | Parte 1 (Agente)      | Parte 2 (API Manual)    | Parte 3 (Colector)           |
| -------------------- | --------------------- | ----------------------- | ---------------------------- |
| Cambios de código    | Ninguno               | Sí (spans, config)      | Sí (igual que Parte 2)       |
| Auto-instrumentación | Sí (HTTP, JDBC, etc.) | Solo lo que se codifica | Solo lo que se codifica      |
| Enrutamiento         | Apps → Jaeger         | Apps → Jaeger           | Apps → Collector → Jaeger    |
| Flexibilidad         | Baja                  | Alta                    | Alta + routing multi-backend |

## Endpoints disponibles

| URL                                     | Descripción                  |
| --------------------------------------- | ---------------------------- |
| `GET http://localhost:8001/courses`     | Lista todos los cursos       |
| `GET http://localhost:8001/{id}`        | Obtiene un curso por ID      |
| `POST http://localhost:8001/courses`    | Crea un curso                |
| `GET http://localhost:8002/catalog`     | Catálogo (llama a CourseApp) |
| `GET http://localhost:8002/firstcourse` | Primer curso via Catalog     |
| `http://localhost:16686`                | Jaeger UI                    |
