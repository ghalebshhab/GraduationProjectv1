# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the pom.xml and download dependencies first (this step is cached)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage (using a smaller JRE image)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Cloud Run sets the PORT environment variable (default 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]