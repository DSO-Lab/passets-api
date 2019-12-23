FROM maven:3.5-jdk-8 AS pre_jar

COPY src/ /src/

COPY pom.xml /

WORKDIR /

RUN mvn package -DskipTests=true

FROM szgx/java:8u111_debian

LABEL maintainer="wimas" version="1.0.0"

ENV TZ="Asia/Shanghai" JAVA_OPTS="" ELASTICSEARCH_URL="http://passets-elasticsearch:9200" ELASTICSEARCH_INDEX="logstash-passets"

COPY --from=pre_jar --chown=0:0 /target/api-1.0.0.jar /api.jar

WORKDIR /

RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

ENTRYPOINT ["bash", "-c", "java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /api.jar"]

EXPOSE 8080