# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install bash and netcat for startup script
RUN apk add --no-cache bash netcat-openbsd

# Set production profile
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the JAR file from build stage
COPY --from=build /app/target/gm-caffe-site-1.0.0.jar app.jar

# Create startup script with database connection retry
RUN printf '#!/bin/bash\n' \
    && printf 'echo "Starting GM Caffe Application..."\n' \
    && printf 'MAX_RETRIES=30\n' \
    && printf 'RETRY_INTERVAL=2\n' \
    && printf 'echo "Waiting for database to be ready..."\n' \
    && printf 'for i in $(seq 1 $MAX_RETRIES); do\n' \
    && printf '  if nc -z $MYSQLHOST $MYSQLPORT 2>/dev/null; then\n' \
    && printf '    echo "Database is ready!"\n' \
    && printf '    break\n' \
    && printf '  fi\n' \
    && printf '  echo "Waiting for database... attempt $i/$MAX_RETRIES"\n' \
    && printf '  sleep $RETRY_INTERVAL\n' \
    && printf 'done\n' \
    && printf 'echo "Starting Java application..."\n' \
    && printf 'exec java -jar -Dserver.port=$PORT app.jar\n' > /start.sh

RUN chmod +x /start.sh

# Expose port
EXPOSE 8080

# Run the application with startup script
ENTRYPOINT ["/start.sh"]
