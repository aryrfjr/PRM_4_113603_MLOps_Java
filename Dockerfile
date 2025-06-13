# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /mlops-api

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src

# Build the jar with tests skipped
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk

WORKDIR /mlops-api

# Copy only the built jar from the build stage
COPY --from=build /mlops-api/target/PRM_4_113603-0.0.1-SNAPSHOT.jar ./mlops-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "mlops-api.jar"]
