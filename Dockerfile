# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install required tools for network check
RUN apk add --no-cache bash nc

# Set production profile
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the JAR file from build stage
COPY --from=build /app/target/gm-caffe-site-1.0.0.jar app.jar

# Create startup script with database connection retry
RUN echo '#!/bin/bash' > /start.sh && \
    echo 'set -e' >> /start.sh && \
    echo 'echo "Starting GM Caffe Application..."' >> /start.sh && \
    echo 'echo "Database host: $MYSQLHOST"' >> /start.sh && \
    echo 'echo "Database port: $MYSQLPORT"' >> /start.sh && \
    echo 'PORT=${PORT:-8080}' >> /start.sh && \
    echo 'echo "Using port: $PORT"' >> /start.sh && \
    echo 'MAX_RETRIES=30' >> /start.sh && \
    echo 'RETRY_INTERVAL=3' >> /start.sh && \
    echo 'for i in $(seq 1 $MAX_RETRIES); do' >> /start.sh && \
    echo '  if nc -z "$MYSQLHOST" "$MYSQLPORT" 2>/dev/null; then' >> /start.sh && \
    echo '    echo "Database is ready!"' >> /start.sh && \
    echo '    break' >> /start.sh && \
    echo '  fi' >> /start.sh && \
    echo '  echo "Waiting for database... attempt $i/$MAX_RETRIES"' >> /start.sh && \
    echo '  sleep $RETRY_INTERVAL' >> /start.sh && \
    echo 'done' >> /start.sh && \
    echo 'echo "Starting Java application on port $PORT..."' >> /start.sh && \
    echo 'exec java -jar -Dserver.port=$PORT -Dserver.address=0.0.0.0 app.jar' >> /start.sh

RUN chmod +x /start.sh

# Expose port (Railway will override with PORT env var)
EXPOSE 8080

# Run the application with startup script
ENTRYPOINT ["/start.sh"]

