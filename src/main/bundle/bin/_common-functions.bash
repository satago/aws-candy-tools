#!/usr/bin/env bash

function aws_account_id {
    # http://stackoverflow.com/a/33791322
    aws ec2 describe-security-groups \
              --group-names 'Default' \
              --query 'SecurityGroups[0].OwnerId' \
              --output text
}

function docker_login_ecr {
  if [[ -z ${DOCKER_REGISTRY_SERVER} ]]
  then
    DOCKER_REGISTRY_SERVER="$(aws_account_id).dkr.ecr.$(aws configure get region).amazonaws.com"
  fi

  aws ecr get-login-password | docker login --username AWS --password-stdin "${DOCKER_REGISTRY_SERVER}"
}

# return "namespace" or empty string from '[namespace/]name'
function extract_namespace_from_fqn {
  echo "$1" | awk '{ n=split($0,arrayIN,"/"); print (n==1 ? "" : arrayIN[1]) }'
}

# return "name" from '[namespace/]name'
function extract_name_from_fqn {
  echo "$1" | awk '{ n=split($0,arrayIN,"/"); print (n==1 ? $0 : arrayIN[2]) }'
}

function safe_fq_name {
  # replace `/` with `-`
  local IN=$1
  echo -n "${IN//\//-}"
}
