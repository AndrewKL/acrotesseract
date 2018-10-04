#!/usr/bin/env bash
# this will build the project and then create a zip with the docker image
# that aws elastic beanstalk needs to run.

sbt docker:stage
echo "EXPOSE 9000" >> ./target/docker/stage/Dockerfile
cp ./Dockerrun.aws.json ./target/docker/stage
( cd ./target/docker/stage ; zip -r ../../acrotesseract-docker.zip ./* )
