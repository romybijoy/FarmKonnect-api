# Stage 1: Build the application with Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only necessary files first to optimize caching
COPY pom.xml .

RUN mvn dependency:go-offline -B  # Pre-download dependencies for faster builds

# Copy the source code AFTER downloading dependencies
COPY src ./src
RUN mvn clean package -DskipTests  # Skip tests for faster builds (optional)

# Stage 2: Use a lightweight JRE for production
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Copy only the built JAR to keep the final image small
COPY --from=builder /app/target/stories_service-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 4053

# Use exec form for better signal handling
ENTRYPOINT ["java", "-jar", "/app.jar"]
