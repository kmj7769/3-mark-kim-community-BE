FROM openjdk:25-ea-24-jdk-slim-bullseye

WORKDIR /app

COPY ./build/libs/community-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]