# Multi-stage build for optimized production image
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre-alpine AS production

# Install curl for health checks
RUN apk add --no-cache curl

# Add non-root user for security
RUN addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Create logs directory and set permissions
RUN mkdir -p /app/logs && chown spring:spring /app/logs

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the jar file with optimized JVM settings
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
