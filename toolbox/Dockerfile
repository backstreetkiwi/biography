FROM eclipse-temurin:17-jdk-focal

RUN apt-get update

# common tools
RUN apt-get install -y unzip

# image/media tools
RUN apt-get install -y apt-transport-https
RUN apt-get install -y libimage-exiftool-perl
RUN apt-get install -y imagemagick
RUN apt-get install -y ffmpeg

# Maven
RUN curl https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.zip > /tmp/maven.zip
RUN unzip /tmp/maven.zip -d /root/
ENV PATH "/root/apache-maven-3.8.8/bin:$PATH"
