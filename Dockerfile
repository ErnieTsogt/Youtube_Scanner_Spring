FROM openjdk:17-alpine

ADD target/praktyki_spring-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]