#!/bin/bash -eux
# Required variables
# FUNCTION_NAME
# AWS_ID
# LAMBDA_ROLE
# LAMBDA_HANDLER
projectName=$(sbt -no-colors name | tail -n1 | cut -d' ' -f2)
projectVersion=$(sbt -no-colors version | tail -n1 | cut -d' ' -f2)

sbt clean && \
sbt assembly && \
aws s3 cp target/scala-2.11/$projectName-assembly-$projectVersion.jar s3://$BUCKET_NAME/ && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME && \
aws lambda update-function-code \
  --function-name $FUNCTION_NAME \
  --s3-bucket $BUCKET_NAME --s3-key $projectName-assembly-$projectVersion.jar

#fileUrl="fileb://target/scala-2.11/${projectName}-assembly-$projectVersion.jar"
#aws lambda create-function \
#  --function-name $FUNCTION_NAME \
#  --zip-file $fileUrl \
#  --role arn:aws:iam::$AWS_ID:role/$LAMBDA_ROLE \
#  --handler $LAMBDA_HANDLER \
#  --runtime java8 \
#  --timeout 30 \
#  --memory-size 512
