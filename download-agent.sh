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
echo ""
echo "  CourseApp:"
echo "    cd parte1-JaegerCourseApp && mvn package -DskipTests"
echo "    java -javaagent:../opentelemetry-javaagent.jar -jar target/FutureXCourseApp-0.0.1-SNAPSHOT.jar"
echo ""
echo "  CourseCatalog:"
echo "    cd parte1-JaegerCourseCatalog && mvn package -DskipTests"
echo "    java -javaagent:../opentelemetry-javaagent.jar -jar target/FutureXCourseCatalog-0.0.1-SNAPSHOT.jar"
