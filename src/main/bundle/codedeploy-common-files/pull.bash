#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

source ${SCRIPT_PATH}/compose.bash

docker_login_ecr

compose pull
