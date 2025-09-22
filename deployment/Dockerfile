# Imagen base: Java 21 LTS, ligera
FROM eclipse-temurin:21-jdk-alpine

# Crear directorio de trabajo
WORKDIR /app

# Copiar JAR al contenedor
COPY applications/app-service/build/libs/RequestsCrediYaMicroservice.jar /app/RequestsCrediYaMicroservice.jar


# Exponer el puerto de la aplicación
EXPOSE 8081

## Configuración de la JVM optimizada para contenedores
#ENV JAVA_OPTS=" -Xshareclasses:name=cacheapp,cacheDir=/cache,nonfatal \
# -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 \
# -Djava.security.egd=file:/dev/./urandom"

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Cambiar a usuario seguro
USER appuser

# Comando de inicio
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar RequestsCrediYaMicroservice.jar"]
ENTRYPOINT ["java", "-jar", "RequestsCrediYaMicroservice.jar"]
