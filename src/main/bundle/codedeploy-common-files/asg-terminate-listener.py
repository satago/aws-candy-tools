#!/usr/bin/python
#

import yaml
import subprocess
import time
import json
import sys
import requests

REGION=sys.argv[1]
QUEUE_URL=sys.argv[2]


#find my instance id
myInstanceId=requests.get("http://169.254.169.254/latest/meta-data/instance-id").text

#wait for terminate message appears in the queue
print "Waiting for termination message in queue", QUEUE_URL
receivedMessage=None
while not receivedMessage:
    sqsMessagesJson = subprocess.check_output(['aws','sqs', 'receive-message', '--region', REGION, '--queue-url', QUEUE_URL])
    if sqsMessagesJson != '':
        sqsMessages=json.loads(sqsMessagesJson)
        for message in sqsMessages['Messages']:
            message['Body'] = json.loads(message['Body'])
            if 'EC2InstanceId' not in message['Body']:
                # delete garbage
                subprocess.check_output(['aws','sqs', 'delete-message', '--region', REGION, '--queue-url', QUEUE_URL, '--receipt-handle', message['ReceiptHandle']])
            elif message['Body']['EC2InstanceId'] == myInstanceId:
                # delete the message from the queue
                receivedMessage = message
                subprocess.check_output(['aws','sqs', 'delete-message', '--region', REGION, '--queue-url', QUEUE_URL, '--receipt-handle', receivedMessage['ReceiptHandle']])
                break
    if not receivedMessage:
        time.sleep(5)
print "Received termination message"

#execute application stop scripts configured in appspec.xml
appspec = yaml.safe_load(open('appspec.yml', 'r'))
if 'ApplicationStop' in appspec['hooks']:
    for hook in appspec['hooks']['ApplicationStop']:
        timeout = hook['timeout']
        startTime = time.time()
        print "Executing", hook['location']
        task = subprocess.Popen(['./'+hook['location']], env={'LIFECYCLE_EVENT':'ApplicationStop'})
        while task.poll() is None:
            if time.time() - startTime > timeout:
                print "Timeouted"
                break
            time.sleep(1)

#confirm to access group that scaling down can proceed
print "Confirming lifecycle action to autoscaling group"
subprocess.check_output(['aws', 'autoscaling', 'complete-lifecycle-action', '--region', REGION, '--lifecycle-action-result', 'CONTINUE',
    '--lifecycle-hook-name', message['Body']['LifecycleHookName'], '--auto-scaling-group-name', message['Body']['AutoScalingGroupName'], '--instance-id', myInstanceId])

print "Termininate instance finished"