#!/bin/bash
docker run \
    --rm -it \
    -v $(pwd):/biography \
    -v ~/.m2:/var/maven/.m2 \
    --workdir /biography \
    -u $(id -u):$(id -g) \
    -e MAVEN_CONFIG=/var/maven/.m2 \
    de.nikolauswinter/biography-toolbox:latest \
    /bin/bash
