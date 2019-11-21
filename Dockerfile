FROM szgx/java:8u111_debian

LABEL maintainer="wimas"

ENV API_VER="1.0.0" TZ="Asia/Shanghai" JAVA_OPTS="" WAIT_SERVICE="passets-elasticsearch:9200" ELASTICSEARCH_URL="http://passets-elasticsearch:9200" ELASTICSEARCH_INDEX="passets-syslog"

VOLUME /tmp

WORKDIR /

RUN curl -L https://github.com/DSO-Lab/passets-api/releases/download/${API_VER}/api-${API_VER}.jar -o api.jar && \
    curl -L https://github.com/vishnubob/wait-for-it/raw/master/wait-for-it.sh -o wait-for-it.sh && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
	echo "Asia/Shanghai" > /etc/timezone && \
	chmod +x wait-for-it.sh

ENTRYPOINT ["bash", "-c", "/wait-for-it.sh ${WAIT_SERVICE} -- java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar"]

EXPOSE 8080