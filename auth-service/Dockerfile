# Stage 1: Build application
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only essential files first to optimize caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests  # Skipping tests speeds up builds

# Stage 2: Use a lightweight JRE for production
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Copy only the built JAR, keeping the final image small
COPY --from=builder /app/target/auth-service-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 4051
EXPOSE 9091

# Use exec form of ENTRYPOINT for better signal handling
ENTRYPOINT ["java", "-jar", "/app.jar"]
