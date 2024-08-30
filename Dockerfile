#Build stage

FROM gradle:latest AS BUILD
WORKDIR /usr/app/
COPY . . 
RUN gradle build

# Package stage

FROM openjdk:latest
ENV JAR_NAME=AngryBot-3.0.0-all.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME .
ENTRYPOINT exec java -jar $APP_HOME/build/libs/$JAR_NAME  