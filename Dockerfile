FROM maven:3.8.4-openjdk-11-slim AS build

WORKDIR /app

COPY . /app

#this step is not exactly in the Jenkinsfile
RUN mvn clean package -DskipTests

#repeating the process to get the new jar -- won't need to create jar using utils in Jenkinsfile??
#create the final image
#docker pull aboyanov/openjdk
FROM openjdk

WORKDIR /app

#copy the jar file from the build stage
COPY --from=build /app/target/wikidata-exporter-exec.jar /app/

CMD ["java", "-Xmx512m", "-jar", "wikidata-exporter-exec.jar"]
