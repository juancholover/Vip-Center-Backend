# Etapa de construcción (Build)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila el proyecto saltándose las pruebas para que sea más rápido
RUN mvn clean package -DskipTests

# Etapa de ejecución (Run)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copia el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
# Expone el puerto dinámico
EXPOSE 8080
# Comando para ejecutar la aplicación con límite de memoria RAM (Crucial para Render Free)
ENTRYPOINT ["java", "-Xmx300m", "-Xms300m", "-jar", "app.jar"]
