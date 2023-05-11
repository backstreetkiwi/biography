#!/bin/bash
docker run \
    --rm \
    -v $(pwd):/biography \
    -v ~/.m2:/var/maven/.m2 \
    --workdir /biography \
    -u $(id -u):$(id -g) \
    -e MAVEN_CONFIG=/var/maven/.m2 \
    maven:3-eclipse-temurin-17-focal \
    mvn -Duser.home=/var/maven clean compile
