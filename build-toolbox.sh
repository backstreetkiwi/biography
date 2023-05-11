#!/bin/bash
pushd toolbox/
docker build . -t de.nikolauswinter/biography-toolbox:latest
popd