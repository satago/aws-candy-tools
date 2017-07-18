#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

source ${SCRIPT_PATH}/compose.bash

$(aws ecr $(docker_login_options) get-login)

compose pull
