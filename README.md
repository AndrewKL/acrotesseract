#Architecture#

DNS ->CDN -> AWS Elastic Beanstalk -> docker(w/ play) -> RDS(mysql)

#building jar#

Download SBT and run 
```
sbt clean
sbt docker:stage  
echo "EXPOSE 9000" >> ./target/docker/stage/Dockerfile
cp ./Dockerrun.aws.json ./target/docker/stage
( cd ./target/docker/stage ; zip -r ../../acrotesseract-docker.zip ./* )
```
This will generate the docker container zip that can run in aws elastic beanstalk.