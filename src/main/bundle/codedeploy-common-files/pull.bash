#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

source ${SCRIPT_PATH}/compose.bash
source ${SCRIPT_PATH}/_common-functions.bash

aws ecr get-login-password | docker login --username AWS --password-stdin $(docker_login_options) $(docker_server)

compose pull
