#!/usr/bin/env bash
#apk update && apk add --no-cache jq

awsc() {
  aws --no-sign-request --endpoint-url 'http://localhost:4566' --region 'eu-central-1' $*
}

sqs() {
  awsc sqs $*
}

createqueue() {
  sqs create-queue --queue-name $1
}

QUEUES="default-sample-queue-name"
for queue in $QUEUES; do
  createqueue $queue
done
