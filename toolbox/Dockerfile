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

# Maven should be used by all users
RUN cp -r /root/apache-maven-3.8.8 /usr/share

# Maven scripts
COPY mvn-compile /var/mvn/mvn-compile
RUN chmod a+x /var/mvn/mvn-compile
COPY mvn-test /var/mvn/mvn-test
RUN chmod a+x /var/mvn/mvn-test
COPY mvn-package /var/mvn/mvn-package
RUN chmod a+x /var/mvn/mvn-package

# Maven and Scripts on the path
ENV PATH "/var/mvn:/usr/share/apache-maven-3.8.8/bin:$PATH"