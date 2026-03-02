FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    mv target/gm-caffe-site-1.0.0.jar app.jar && \
    rm -rf src target
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
