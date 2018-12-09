FROM maven:3.6.0-jdk-8-alpine AS build
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/app/target/config-service-0.0.1-SNAPSHOT.jar /usr/app/
COPY --from=build /usr/src/app/src/main/resources/myConfig /usr/src/app/src/main/resources/myConfig
EXPOSE 8888
CMD ["java","-jar","/usr/app/config-service-0.0.1-SNAPSHOT.jar","--spring.profiles.active=docker"]
