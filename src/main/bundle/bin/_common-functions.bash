#!/usr/bin/env bash

# Retries a command a with backoff.
#
# The retry count is given by ATTEMPTS (default 5), the
# initial backoff timeout is given by TIMEOUT in seconds
# (default 1.)
#
# Successive backoffs double the timeout.
#
# Beware of set -e killing your whole script!
function with_backoff {
  local max_attempts=${ATTEMPTS-5}
  local timeout=${TIMEOUT-1}
  local attempt=0
  local exitCode=0

  while [[ $attempt < $max_attempts ]]
  do
    "$@"
    exitCode=$?

    if [[ $exitCode == 0 ]]
    then
      break
    fi

    echo "Failure! Retrying in $timeout.." 1>&2
    sleep $timeout
    attempt=$(( attempt + 1 ))
    timeout=$(( timeout * 2 ))
  done

  if [[ $exitCode != 0 ]]
  then
    echo "You've failed me for the last time! ($@)" 1>&2
  fi

  return $exitCode
}

function aws_account_id {
    # http://stackoverflow.com/a/33791322
    with_backoff aws ec2 describe-security-groups \
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
