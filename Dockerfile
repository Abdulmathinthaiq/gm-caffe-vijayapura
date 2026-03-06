# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Set production profile
ENV SPRING_PROFILES_ACTIVE=prod

# Print environment variables for debugging (remove in production)
RUN echo "=== Environment Variables ===" && \
    env | grep -E "(MYSQL|DB_|SPRING)" || true

# Copy the JAR file from build stage
COPY --from=build /app/target/gm-caffe-site-1.0.0.jar app.jar
COPY startup.sh /startup.sh
RUN chmod +x /startup.sh

# Create static directory and copy static files/templates
RUN mkdir -p /app/static /app/templates
COPY --from=build /app/src/main/resources/static /app/static
COPY --from=build /app/src/main/resources/templates /app/templates

# Expose port
EXPOSE 8080

# Run the application with debug script
ENTRYPOINT ["/startup.sh"]
