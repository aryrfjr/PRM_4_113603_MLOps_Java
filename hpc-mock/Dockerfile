# TODO: move src to a new folder hpc-mock?
# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /hpc-mock

# Copy everything from the context (hpc-mock/)
COPY . .

# Make Maven wrapper executable and cache dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Build the JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk

WORKDIR /hpc-mock

# Copy only the final jar
COPY --from=build /hpc-mock/target/PRM_4_113603-0.0.1-SNAPSHOT.jar ./hpc-mock.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "hpc-mock.jar"]
