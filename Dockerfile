FROM hub.c.163.com/library/java:latest
VOLUME /tmp
ADD target/*.jar app.jar
EXPOSE 8080
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]