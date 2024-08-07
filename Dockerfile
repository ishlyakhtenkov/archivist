FROM openjdk:17-jdk-alpine
EXPOSE 8080
ARG JAR_FILE=target/archivist.jar
ARG CONTENT_DIR=content
ADD ${JAR_FILE} archivist.jar
ADD content content
ENTRYPOINT ["java","-jar","/archivist.jar"]
