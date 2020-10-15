FROM adoptopenjdk/openjdk11
ARG JAR_FILE=target/typeahead-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} typeahead.jar
ENTRYPOINT ["java","-jar","/typeahead.jar"]