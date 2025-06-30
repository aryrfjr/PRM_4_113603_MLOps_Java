# TODO: move src to a new folder mlops-api?
# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /mlops-api

# GitHub authentication for packages
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

# To allow Maven to use in settings.xml
ENV MAVEN_USERNAME=${GITHUB_USERNAME}
ENV MAVEN_PASSWORD=${GITHUB_TOKEN}

# Copy everything from the context (mlops-api/)
COPY . .
COPY settings.xml /root/.m2/settings.xml

# Make Maven wrapper executable and cache dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Build the JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jdk

WORKDIR /mlops-api

# Copy only the final jar
COPY --from=build /mlops-api/target/PRM_4_113603-0.0.1-SNAPSHOT.jar ./mlops-api.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "mlops-api.jar"]
