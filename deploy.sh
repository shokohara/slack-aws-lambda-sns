#!/bin/bash -eux
# Required variables
# FUNCTION_NAME
# AWS_ID
# LAMBDA_ROLE
# LAMBDA_HANDLER
projectName=$(sbt -no-colors name | tail -n1 | cut -d' ' -f2)

sbt clean && \
sbt assembly && \
aws s3 cp target/scala-2.11/$projectName-assembly-0.1-SNAPSHOT.jar s3://$BUCKET_NAME/ && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --s3-bucket $S3_BUCKET --s3-key $projectName-assembly-0.1-SNAPSHOT.jar

#fileUrl="fileb://target/scala-2.11/${projectName}-assembly-0.1-SNAPSHOT.jar"
#aws lambda create-function \
#  --function-name $FUNCTION_NAME \
#  --zip-file $fileUrl \
#  --role arn:aws:iam::$AWS_ID:role/$LAMBDA_ROLE \
#  --handler $LAMBDA_HANDLER \
#  --runtime java8 \
#  --timeout 30 \
#  --memory-size 512
