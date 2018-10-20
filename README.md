# Architecture

DNS ->CDN -> AWS Elastic Beanstalk -> docker(w/ play) -> RDS(mysql)

# Building jar

Download sbt and run the aws-package.sh shell script.  This will generate the docker container zip that can run in aws elastic beanstalk.

# Connecting and migrating RDS

We use AWS RDS w/ mysql for the DB.  The password and username is store in secrets manager.  To access this first create a user with permission to access the secret and add the aws credentials to that user to your ~/.aws/credentials as the default profile.
 
Then run the Migrate integration test.

# How to run a container locally

First build the aws docker container using the aws-package.sh script.  Then run the following commands

```
➜  AcroTesseract git:(master) ✗ docker build ./target/docker/stage/          
...
Successfully built f3ffe67178d3
➜  AcroTesseract git:(master) ✗ docker container run  -e AWS_ACCESS_KEY_ID=xyz -e AWS_SECRET_ACCESS_KEY=aaa f3ffe67178d3
```

# Debugging elastic beanstalk container

SSH into the host with the acrotesseract.pem file

cd into

>/var/log/eb-docker/containers/eb-current-app

There should be a randomly named log file that looks like..

> /var/log/eb-docker/containers/eb-current-app/937e1b7b26ef-stdouterr.log

that contains plays logs