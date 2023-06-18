# Build stage
FROM maven:3.8.5-openjdk-18 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:18-jdk
COPY --from=build /app/target/*.jar app.jar
RUN echo -e '#!/bin/bash\njava -jar /app.jar | awk "{print \$0}"' > start-app.sh && chmod +x start-app.sh
CMD ["./start-app.sh"]
