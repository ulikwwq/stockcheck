FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY . .

RUN ./gradlew clean bootJar --no-daemon

EXPOSE 8080

CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]