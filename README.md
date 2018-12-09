# **Dockerizing a Spring Boot Microservices Application:**
---
Once we have all our services ready and running perfectly on the local machine, we will be creating an individual container for each service, as shown in the microservice architecture diagram : 
<br/> 
![First Image](/img1.png?raw=true "Application Architecture") 

Below is the list of containers for our project:

1. Config Service
2. Discovery Service
3. Product Service
4. Proxy Service

We will be using Docker to build a container image of each of our microservices as a part of the Maven build process. Afterward , we can easily orchestrate the full microservice cluster on our own machine using Docker compose.

# Docker Configuration for the Config Service
***
The container should contain the config server jar file. Here, we will pick the jar file from the local machine. The config server should be available on the port 8888 as per the required properties.

We go under the project’s root directory and create a new file named _‘Dockerfile_ ’, in order to define a docker image.
```
$ cd config -service
$ touch Dockerfile
```

Dockerfile is where we define the docker image and specify all the configurations required to run the app. Following is the Dockerfile for our config service:
```
FROM maven:3.6.0-jdk-8-alpine AS build
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/app/target/config -service -0. 0.1 -SNAPSHOT.jar  /usr/app/
COPY --from=build /usr/src/app/src/main/resources/myConfig /usr/src/app/src/main/resources/myConfig
EXPOSE 8888
CMD ["java"," -jar","/usr/app/config-service-0.0.1-SNAPSHOT.jar "," --spring.profiles.active=docker"]
```
We add the following file _‘application -docker.properties ’_ to _‘\config -service\src\main\resources ’:_
```
spring.cloud.config.server.git.uri=file:/usr/src/ app/src/main/resources/myConfig
```
Now that we have defined the Dockerfile, let’s build a docker image for our application. We type the following command from the root directory of the config service project to build the docker image:
```
$ docker build --file=Dockerfile --tag=config-service:latest --rm=true .
```
Now let us create a Docker volume:
```
$ docker volume create --name=config-repo
```
To run the above generated image, we run the following command:
```
$ docker run --name=config-service --publish= 8888 :8888 --volume=myConfig:/usr/src/app/src/main/resources/myConfig  config-service:latest
```
Once we run the above command, we should be able to see a Docker container up and be running.

# Docker Configuration for the Discovery Service
***
Similarly, we need to create a Docker file for EurekaServer, which will be running on port 8761. The ‘ _Dockerfile_ ’ for Discovery Service should be as below:

```
FROM maven:3.6.0-jdk-8-alpine AS build
WORKDIR /usr/src/app
COPY pom.xml  .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/app/target/discovery-service-0.0.1-SNAPSHOT.jar /usr/app/
EXPOSE 8761
RUN apk add --no-cache bash
ADD wait-for-it.sh /wait-for-it.sh
CMD ["java"," -jar","/usr/app/discovery-service-0.0.1-SNAPSHOT.jar"," --spring.profiles.active=docker"]
```
Now, we need to add the following 2 file s _‘application -docker.properties ’_ and _‘bootstrap.yml ’_ to _‘\discovery-service\src\main\resources ’_ :

**application -docker.properties :**
```
eureka.instance.hostname=discovery-service
```
**bootstrap.yml:** 
<br/> 
![Second Image](/img2.png?raw=true "Bootstrap File")
A script _“wait-for-it.sh”_ is also added under the root directory of the discovery service. It is a pure bash script that will wait on the availability of a host and TCP port. This will be useful for synchronizing the spin-up of our linked docker containers and control the order of the service startup.

Now, we use this command to build the image :
```
$ docker build --file=Dockerfile --tag =discovery-service:latest --rm=true .
```
We run the generated image like below:
```
$ docker run --name=discovery-service --publish=8761:8761 discovery-service:latest
```
# Docker Configuration for the Product and the Proxy Service 
***
Now , it is time to deploy our actual Microservices. The steps should be similar; the only thing we need to remember is our microservices are dependent on ConfigService and Discovery Service , so we always need to make sure that before we start our microservices, the above two are up and running. There are dependencies involved among containers, so it is time to explore Docker Compose. It's a beautiful way to make sure that containers are being spawned and maintaining a certain order.

To do that, we should write up a ‘ _Dockerfile ’_ for the rest of the containers as follows:


**Dockerfile for the ProductService:**
```
FROM maven:3.6.0-jdk-8-alpine AS build
WORKDIR /usr/src/app
COPY pom.xml  .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/app/target/product-service.0.0.1-SNAPSHOT.jar  /usr/app/
EXPOSE 8080
RUN apk add --no-cache bash
ADD wait-for-it.sh /wait-for-it.sh
CMD ["java"," -jar","/usr/app/product-service.0.0.1-SNAPSHOT.jar"," --spring.profiles.active=docker"]
```
**Dockerfile for the ProxyService:**
```
FROM maven:3. 6.0-jdk-8-alpine AS build
WORKDIR /usr/src/app
COPY pom.xml  .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:8-jre-alpine
COPY --from=build /usr/src/app/target/proxy-service.0.0.1-SNAPSHOT.jar  /usr/app/
EXPOSE 9999
RUN apk add --no-cache bash
ADD wait-for-it.sh /wait-for-it.sh
CMD ["java"," -jar","/usr/app/proxy-service.0.0.1-SNAPSHOT.jar"," --spring.profiles.active=docker"]
```
Similarly, to what we did for the discovery service , we add the “ _wait-for-it.sh_ ” script under the the root directory of each of these microservices.

Then, under the ‘ _resources_ ’ folder of each microservices (Proxy and Product services), we add the following 2 files:
**application -docker.properties :**
```
spring.cloud.config.uri = [http://config-service:8888](http://config-service:8888)
```
**bootstrap.yml:**
<br/> 
![Third Image](/img3.png?raw=true "Bootstrap File")

Now let us create a file called _‘docker-compose.yml’_ , which will use all these Dockerfiles to spawn our required environments. It will also make sure that the required containers being spawned are maintaining correct order and they are interlinked.

Under the root directory of our project, we add the following file ‘ _docker-compose.yml_ ’ : 
<br/> 
![Fourth Image](/img4.png?raw=true "Compose File")

Similarly, to what we did before, we add the “ _wait-for-it.sh_ ” script under this same directory as below: 
<br/> 
![Fifth Image](/img5.png?raw=true "Directory Hierarchy")

After creating the file, let us build our images, create the required containers, and start with a single command:
```
$ docker-compose up --build
```
To stop the complete environment, we can use this command:
```
$ docker-compose down
```
All what we need to do now is to check the IP on which Docker default machine is running. The IP can be found once you open the docker terminal:
<br/> 
![Sixth Image](/img6.png?raw=true "Docker IP")

Now, we can verify that our application is running.


**P.S:** Please note that to test all the links that were locally running properly, you need to use 192.168.99.100 instead of localhost.

# References:
***
[https://www.callicoder.com/spring-boot-docker-example/](https://www.callicoder.com/spring-boot-docker-example/)
[https://dzone.com/articles/buiding-microservice-using-spring-boot-and-docker?fromrel=true](https://dzone.com/articles/buiding-microservice-using-spring-boot-and-docker?fromrel=true)
[https://gist.github.com/eyablokov/9b167d719821c9148c339d5ea3c0e6d](https://gist.github.com/eyablokov/9b167d719821c9148c339d5ea3c0e6d)
[https://docs.docker.com/](https://docs.docker.com/)


