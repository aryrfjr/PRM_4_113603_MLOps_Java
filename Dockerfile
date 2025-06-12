# === STAGE 1: Build ===
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /mlops-api
COPY . .
RUN mvn clean package -DskipTests

# === STAGE 2: Runtime ===
# Use JDK 17 base image
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /mlops-api

# Copy the built jar (assumes it is built before)
COPY --from=build /mlops-api/target/*.jar mlops-api.jar

# Expose the port (optional)
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "mlops-api.jar"]

