FROM de.nikolauswinter/biography-toolbox:latest AS build
COPY .. /project
WORKDIR /project
RUN mvn clean package

FROM build AS runtime
RUN apt-get update
RUN apt-get install -y apt-transport-https
RUN apt-get install -y libimage-exiftool-perl
RUN apt-get install -y imagemagick
RUN apt-get install -y ffmpeg
COPY --from=build /project/target/biography-*.jar /app.jar

# TODO Config file(s)
# Run Java w/ SpringBoot application
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]