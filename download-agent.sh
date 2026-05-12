#!/bin/bash
set -e

AGENT_JAR="opentelemetry-javaagent.jar"

if [ -f "$AGENT_JAR" ]; then
    echo "El agente ya existe: $AGENT_JAR"
    exit 0
fi

echo "Descargando OpenTelemetry Java Agent..."
curl -L -o "$AGENT_JAR" \
    "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar"

echo "Agente descargado exitosamente: $AGENT_JAR"
echo ""
echo "Para ejecutar las aplicaciones con el agente:"
echo "(Los flags -D tienen prioridad sobre application.properties y el agente los lee antes de inicializar)"
echo ""
echo "  CourseApp:"
echo "    cd parte1-JaegerCourseApp && mvn package -DskipTests && cd .."
echo "    java -javaagent:opentelemetry-javaagent.jar \\"
echo "         -Dotel.service.name=fx-course-service \\"
echo "         -Dotel.exporter.otlp.endpoint=http://localhost:4317 \\"
echo "         -Dotel.exporter.otlp.protocol=grpc \\"
echo "         -Dotel.traces.exporter=otlp \\"
echo "         -Dotel.metrics.exporter=none \\"
echo "         -Dotel.logs.exporter=none \\"
echo "         -jar parte1-JaegerCourseApp/target/FutureXCourseApp-0.0.1-SNAPSHOT.jar"
echo ""
echo "  CourseCatalog:"
echo "    cd parte1-JaegerCourseCatalog && mvn package -DskipTests && cd .."
echo "    java -javaagent:opentelemetry-javaagent.jar \\"
echo "         -Dotel.service.name=fx-catalog-service \\"
echo "         -Dotel.exporter.otlp.endpoint=http://localhost:4317 \\"
echo "         -Dotel.exporter.otlp.protocol=grpc \\"
echo "         -Dotel.traces.exporter=otlp \\"
echo "         -Dotel.metrics.exporter=none \\"
echo "         -Dotel.logs.exporter=none \\"
echo "         -jar parte1-JaegerCourseCatalog/target/FutureXCourseCatalog-0.0.1-SNAPSHOT.jar"
