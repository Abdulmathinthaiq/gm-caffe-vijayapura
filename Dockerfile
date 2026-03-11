# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Set production profile - Railway will provide PORT via environment variable
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

# Create necessary directories
RUN mkdir -p /app/static /app/templates /app/static/uploads

# Copy the JAR file from build stage
COPY --from=build /app/target/gm-caffe-site-1.0.0.jar app.jar

# Copy startup script
COPY startup.sh /startup.sh
RUN chmod +x /startup.sh

# Copy static files and templates from build stage
COPY --from=build /app/src/main/resources/static /app/static
COPY --from=build /app/src/main/resources/templates /app/templates

# Expose port
EXPOSE 8080

# Health check - increased start period for Spring Boot startup
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/ || exit 1

# Run the application
ENTRYPOINT ["/startup.sh"]
