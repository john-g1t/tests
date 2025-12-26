FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY --chown=gradle:gradle src ./src

RUN gradle clean war --no-daemon --info

FROM tomcat:10.1-jdk21-temurin

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/build/libs/*.war /usr/local/tomcat/webapps/api.war

RUN mkdir -p /usr/local/tomcat/logs

ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 8080

WORKDIR /usr/local/tomcat

CMD ["catalina.sh", "run"]
