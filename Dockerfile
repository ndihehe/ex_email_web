FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


FROM tomcat:9.0-jdk21-temurin-jammy

# Tắt port shutdown 8005 để Render không gửi request nhầm cổng
RUN sed -i 's/port="8005"/port="-1"/g' /usr/local/tomcat/conf/server.xml

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=build /app/target/email-list.war /usr/local/tomcat/webapps/ROOT.war

# Ép Java dùng IPv4 để kết nối Gmail SMTP tức thì trên môi trường Docker/Render
ENV JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false"

EXPOSE 8080

CMD ["catalina.sh", "run"]
