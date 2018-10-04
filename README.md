#Architecture#

DNS ->CDN -> AWS Elastic Beanstalk -> docker(w/ play) -> RDS(mysql)

#building jar#

Download sbt and run the aws-package.sh shell script.  This will generate the docker container zip that can run in aws elastic beanstalk.

#Connecting and migrating RDS#

We use AWS RDS w/ mysql for the DB.  The password and username is store in secrets manager.  To access this first create a user with permission to access the secret and add the aws credentials to that user to your ~/.aws/credentials as the acrotesseract profile.
 
Then run the Migrate integration test.