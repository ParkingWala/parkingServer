# -------- Build Stage --------
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven descriptor and source code
COPY pom.xml .
COPY src ./src

# Build the Spring Boot jar (skip tests for faster builds)
RUN mvn clean package -DskipTests

# -------- Run Stage --------
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Use JAVA_OPTS if provided
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
