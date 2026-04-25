FROM openjdk:17-slim

LABEL maintainer="ordering-system"
LABEL version="1.0"

ENV APP_HOME=/app
WORKDIR $APP_HOME

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN useradd -m -r -u 1001 appuser && mkdir -p /app/logs && chown -R appuser:appuser /app

# Copy JAR file (built by Maven)
COPY target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
