FROM openjdk:21-jdk-slim As builder

WORKDIR /app

COPY . .

RUN ./gradlew :module-api:bootJar

FROM openjdk:21-jdk-slim
WORKDIR /app

COPY --from=builder /app/module-api/build/libs/module-api-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]