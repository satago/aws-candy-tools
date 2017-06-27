#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

source ${SCRIPT_PATH}/compose.bash

$(aws ecr get-login --no-include-email)

compose pull
