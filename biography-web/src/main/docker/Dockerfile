FROM openjdk:11-jdk

# SpringBoot application JAR
COPY target/biography-web-*.jar /app.jar

# Install exiftool
RUN apt-get update
RUN apt-get install -y apt-transport-https
RUN apt-get install -y libimage-exiftool-perl
RUN apt-get install -y imagemagick
RUN apt-get install -y ffmpeg

# Run Java w/ SpringBoot application
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.config.location=/etc/biography/config.yml","-jar","/app.jar"]