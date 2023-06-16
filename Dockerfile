# syntax=docker/dockerfile:1
FROM openjdk:17-jdk
# Copy jar to context
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]