#!/bin/bash -eu
# Required variables
# FUNCTION_NAME
# AWS_ID
# LAMBDA_ROLE
# LAMBDA_HANDLER
projectName=$(sbt -no-colors name | tail -n1 | cut -d' ' -f2)

sbt assembly && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --zip-file fileb://target/scala-2.11/$projectName-assembly-0.1-SNAPSHOT.jar

#fileUrl="fileb://target/scala-2.11/${projectName}-assembly-0.1-SNAPSHOT.jar"
#aws lambda create-function \
#  --function-name $FUNCTION_NAME \
#  --zip-file $fileUrl \
#  --role arn:aws:iam::$AWS_ID:role/$LAMBDA_ROLE \
#  --handler $LAMBDA_HANDLER \
#  --runtime java8 \
#  --timeout 30 \
#  --memory-size 512
