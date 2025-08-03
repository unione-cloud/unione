FROM openjdk:21-jdk
MAINTAINER Jeking Yang

# setting time zone
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

COPY unione-*.jar /usr/local/unione.jar

EXPOSE 8080
ENV JAVA_OPTS=$JAVA_OPTS


ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /usr/local/unione.jar"]
