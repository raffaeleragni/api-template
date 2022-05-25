FROM eclipse-temurin:18-focal
ARG APP_VERSION

RUN mkdir /app
WORKDIR /app
ADD docker-entrypoint.sh /app

RUN curl -Lo /app/dd-java-agent.jar https://dtdg.co/latest-java-tracer

ADD target/*.jar /app/app.jar

ENV APP_VERSION ${APP_VERSION}
ENV DD_VERSION ${APP_VERSION}
EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]

