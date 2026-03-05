# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY target/target/gm-caffe-site-1.0.0.jar app.jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Set production profile
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the JAR file from build stage
COPY --from=build /app/target/gm-caffe-site-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
