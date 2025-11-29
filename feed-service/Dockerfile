# Stage 1: Build the application using Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy only pom.xml first (better build caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Use lightweight JRE image for running
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:+UseG1GC"

# Copy only the built JAR to keep image small
COPY --from=builder /app/target/feed-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4055

ENTRYPOINT ["java", "-jar", "app.jar"]
