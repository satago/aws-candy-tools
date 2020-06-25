#!/usr/bin/env bash

function aws_account_id {
    # http://stackoverflow.com/a/33791322
    echo $(aws ec2 describe-security-groups \
                --group-names 'Default' \
                --query 'SecurityGroups[0].OwnerId' \
                --output text)
}

function docker_login_ecr {
    aws ecr get-login-password | docker login --username AWS --password-stdin $(aws_account_id).dkr.ecr.$(aws configure get region).amazonaws.com
}
