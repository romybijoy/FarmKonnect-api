# Stage 1: Build the application with Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only pom.xml first for better build caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app with lightweight JRE
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Eureka needs more memory than other services
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -XX:+UseG1GC"

COPY --from=builder /app/target/eureka-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
